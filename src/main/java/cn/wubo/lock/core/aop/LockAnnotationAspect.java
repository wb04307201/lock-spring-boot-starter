package cn.wubo.lock.core.aop;


import cn.wubo.lock.core.lock.ILock;
import cn.wubo.lock.exception.LockRuntimeException;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Aspect
public class LockAnnotationAspect {

    CopyOnWriteArrayList<ILock> locks = new CopyOnWriteArrayList<>();

    public LockAnnotationAspect(List<ILock> locks) {
        this.locks = new CopyOnWriteArrayList<>(locks);
    }

    @Pointcut(value = "@annotation(cn.wubo.lock.core.aop.Locking)")
    public void pointCut() {
    }

    @Before("pointCut()")
    public void before(JoinPoint joinPoint) {
        System.out.println("前置通知:" + joinPoint);
        Locking locking = getLocking(joinPoint);
        ILock lock = getLock(locking.alias());
        if (locking.time() > 0 && locking.unit() != null)
            lock.tryLock(locking.alias() + "_" + locking.key(), locking.time(), locking.unit());
        else lock.tryLock(locking.alias() + "_" + locking.key());
    }

    @After("pointCut()")
    public void after(JoinPoint joinPoint) {
        System.out.println("后置通知:" + joinPoint);
        Locking locking = getLocking(joinPoint);
        getLock(locking.alias()).unLock(locking.alias() + "_" + locking.key());
    }

    private Locking getLocking(JoinPoint joinPoint) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Locking locking = methodSignature.getMethod().getAnnotation(Locking.class);
        if (locking == null) locking = methodSignature.getClass().getAnnotation(Locking.class);
        return locking;
    }

    private ILock getLock(String alias) {
        return locks.stream().filter(lock -> lock.support(alias)).findAny().orElseThrow(() -> new LockRuntimeException("未定义的的缓存别名!"));
    }

}
