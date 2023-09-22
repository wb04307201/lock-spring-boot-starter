package cn.wubo.lock.core.aop;


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
        log.debug("LockAnnotationAspect 前置通知:" + joinPoint);
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Locking locking = getLocking(methodSignature);
        log.debug("LockAnnotationAspect method{} alias:{} key:{} 尝试加锁", methodSignature.getMethod().getName(), locking.alias(), locking.keys());
        ILock lock = getLock(locking.alias());
        String newKey = getNewKey(locking.alias(), locking.keys(), joinPoint.getTarget(), methodSignature.getMethod(), joinPoint.getArgs());
        Boolean tryLock = locking.time() > 0 ? lock.tryLock(newKey, locking.time(), locking.unit()) : lock.tryLock(newKey);
        log.debug("LockAnnotationAspect method{} alias:{} key:{} 加锁结果：{}", methodSignature.getMethod().getName(), locking.alias(), locking.keys(), tryLock);
        if (Boolean.FALSE.equals(tryLock))
            throw new LockRuntimeException(String.format("alias:%s key:%s 已存在锁,不能执行！", locking.alias(), Arrays.toString(locking.keys())));
    }

    @After("pointCut()")
    public void after(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Locking locking = getLocking(methodSignature);
        log.debug("LockAnnotationAspect method{} alias:{} key:{} 解锁", methodSignature.getMethod().getName(), locking.alias(), locking.keys());
        getLock(locking.alias()).unLock(getNewKey(locking.alias(), locking.keys(), joinPoint.getTarget(), methodSignature.getMethod(), joinPoint.getArgs()));
    }

    private Locking getLocking(MethodSignature methodSignature) {
        Locking locking = methodSignature.getMethod().getAnnotation(Locking.class);
        if (locking == null) locking = methodSignature.getClass().getAnnotation(Locking.class);
        return locking;
    }

    private ILock getLock(String alias) {
        return locks.stream().filter(lock -> lock.support(alias)).findAny().orElseThrow(() -> new LockRuntimeException("未定义的的缓存别名!"));
    }

    private String getNewKey(String alias, String[] keys, Object rootObject, Method method, Object[] args) {
        return alias.concat(":").concat(getSpelDefinitionKey(keys, rootObject, method, args));
    }

    private String getSpelDefinitionKey(String[] keys, Object rootObject, Method method, Object[] args) {
        StandardEvaluationContext context = new MethodBasedEvaluationContext(rootObject, method, args, NAME_DISCOVERER);
        context.setBeanResolver(beanResolver);
        return Arrays.stream(keys).map(key -> PARSER.parseExpression(key).getValue(context, String.class)).collect(Collectors.joining(":"));
    }

}
