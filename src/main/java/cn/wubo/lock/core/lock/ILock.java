package cn.wubo.lock.core.lock;

import cn.wubo.lock.core.fail.AbstractLockFail;

import java.util.concurrent.TimeUnit;

public interface ILock {

    Boolean support(String alias);

    Boolean tryLock(String key);

    Boolean tryLock(String key, Long time, TimeUnit unit);

    void unLock(String key);

    Integer getRetryCount();

    Long getWaitTime();

    AbstractLockFail getLockFail();
}
