package cn.wubo.lock.core.aop;


import cn.wubo.lock.core.fail.AbstractLockFail;
import cn.wubo.lock.core.lock.ILock;
import cn.wubo.lock.exception.LockRuntimeException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MethodBasedEvaluationContext;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import org.springframework.expression.BeanResolver;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Slf4j
@Aspect
public class LockAnnotationAspect {

    private static final ParameterNameDiscoverer NAME_DISCOVERER = new DefaultParameterNameDiscoverer();
    private static final ExpressionParser PARSER = new SpelExpressionParser();
    private final CopyOnWriteArrayList<ILock> locks;
    private final BeanResolver beanResolver;

    public LockAnnotationAspect(List<ILock> locks, BeanFactory beanFactory) {
        this.locks = new CopyOnWriteArrayList<>(locks);
        this.beanResolver = new BeanFactoryResolver(beanFactory);
    }

    @Pointcut(value = "@annotation(cn.wubo.lock.core.aop.Locking)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 根据方法签名获取Locking对象
        Locking locking = getLocking(methodSignature);
        // 根据Locking对象的别名获取锁对象
        ILock lock = getLock(locking.alias());
        // 如果锁对象不为空，则尝试执行锁定逻辑
        if (lock != null) tryLock(locking, lock, methodSignature, joinPoint.getTarget(), joinPoint.getArgs());
    }


    @After("pointCut()")
    public void after(JoinPoint joinPoint) {
        // 获取当前线程id
        Long threadId = Thread.currentThread().getId();
        // 获取方法签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        // 根据方法签名获取Locking对象
        Locking locking = getLocking(methodSignature);
        // 根据Locking对象的别名获取锁对象
        ILock lock = getLock(locking.alias());
        // 如果锁对象不为空，则解锁
        if (lock != null) {
            // 获取解锁的新key
            String newKey = getNewKey(locking.alias(), locking.keys(), joinPoint.getTarget(), methodSignature.getMethod(), joinPoint.getArgs());
            // 解锁
            lock.unLock(newKey);
            // 打印日志
            log.debug("LockAnnotationAspect thread:{} method{} alias:{} key:{} 解锁", threadId, methodSignature.getMethod().getName(), locking.alias(), newKey);
        }
    }


    /**
     * 根据方法签名获取对应的Locking对象
     * 如果方法上存在Locking注解，则返回该注解对应的Locking对象
     * 如果方法上不存在Locking注解，则返回类上存在的Locking注解对应的Locking对象
     *
     * @param methodSignature 方法签名对象
     * @return Locking对象
     */
    private Locking getLocking(MethodSignature methodSignature) {
        Locking locking = methodSignature.getMethod().getAnnotation(Locking.class);
        if (locking == null) locking = methodSignature.getClass().getAnnotation(Locking.class);
        return locking;
    }


    /**
     * 根据别名获取锁
     *
     * @param alias 锁的别名
     * @return 锁对象，如果别名不存在则返回null
     */
    private ILock getLock(String alias) {
        return locks.stream().filter(lock -> lock.support(alias)).findAny().orElse(null);
    }


    /**
     * 根据别名和给定的参数生成一个新的键。
     *
     * @param alias      别名
     * @param keys       键的数组
     * @param rootObject 根对象
     * @param method     方法
     * @param args       方法参数
     * @return 生成的新键
     */
    private String getNewKey(String alias, String[] keys, Object rootObject, Method method, Object[] args) {
        return alias.concat(":").concat(getSpelDefinitionKey(keys, rootObject, method, args));
    }


    /**
     * 获取SPEL定义的键
     *
     * @param keys       键数组
     * @param rootObject 根对象
     * @param method     方法
     * @param args       参数
     * @return SPEL定义的键
     */
    private String getSpelDefinitionKey(String[] keys, Object rootObject, Method method, Object[] args) {
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, NAME_DISCOVERER);
        context.setBeanResolver(beanResolver);
        return Arrays.stream(keys).map(key -> PARSER.parseExpression(key).getValue(context, String.class)).collect(Collectors.joining(":"));
    }


    private void tryLock(Locking locking, ILock lock, MethodSignature methodSignature, Object target, Object[] args) {
        // 获取当前线程id
        Long threadId = Thread.currentThread().getId();
        // 获取新的锁键值
        String newKey = getNewKey(locking.alias(), locking.keys(), target, methodSignature.getMethod(), args);
        // 尝试加锁
        log.debug("LockAnnotationAspect thread:{} method{} alias:{} key:{} 尝试加锁", threadId, methodSignature.getMethod().getName(), locking.alias(), newKey);
        // 判断是否设置超时时间
        Boolean tryLock = locking.time() > 0 ? lock.tryLock(newKey, locking.time(), locking.unit()) : lock.tryLock(newKey);
        // 如果加锁失败
        if (Boolean.FALSE.equals(tryLock)) {
            // 加锁失败日志
            log.debug("LockAnnotationAspect thread:{} method{} alias:{} key:{} 加锁失败", threadId, methodSignature.getMethod().getName(), locking.alias(), newKey);
            int count = 0;
            // 如果允许重试且重试次数未达到限制或者重试次数设置为-1
            while (Boolean.FALSE.equals(tryLock) && ((lock.getRetryCount() > 0 && count < lock.getRetryCount()) || lock.getRetryCount() < 0)) {
                // 重试计数加1
                count++;
                try {
                    // 线程休眠等待
                    Thread.sleep(lock.getWaitTime());
                } catch (InterruptedException e) {
                    throw new LockRuntimeException(e.getMessage(), e);
                }
                // 重试日志
                log.debug("LockAnnotationAspect thread:{} method{} alias:{} key:{} 加锁失败 第{}次重试", threadId, methodSignature.getMethod().getName(), locking.alias(), newKey, count);
                // 尝试加锁
                tryLock = locking.time() > 0 ? lock.tryLock(newKey, locking.time(), locking.unit()) : lock.tryLock(newKey);
            }
            // 如果加锁仍然失败
            if (Boolean.FALSE.equals(tryLock)) {
                // 失败处理
                fail(locking, lock, newKey, args);
            }
        }
        // 加锁成功日志
        log.debug("LockAnnotationAspect thread:{} method{} alias:{} key:{} 加锁", threadId, methodSignature.getMethod().getName(), locking.alias(), newKey);
    }


    /**
     * 失败处理方法
     *
     * @param locking 加锁对象
     * @param lock    锁对象
     * @param newKey  锁键值
     * @param args    方法参数
     */
    private void fail(Locking locking, ILock lock, String newKey, Object[] args) {
        AbstractLockFail lockFail = lock.getLockFail();
        if (lockFail != null) lockFail.fail(args);
        throw new LockRuntimeException(String.format("alias:%s key:%s 已存在锁,不能执行！", locking.alias(), newKey));
    }


}
