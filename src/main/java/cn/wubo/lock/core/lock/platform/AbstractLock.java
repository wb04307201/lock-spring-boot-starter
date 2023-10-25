package cn.wubo.lock.core.lock.platform;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.fail.AbstractLockFail;
import cn.wubo.lock.core.lock.ILock;

public abstract class AbstractLock implements ILock {

    protected LockAliasProperties lockAliasProperties;
    protected AbstractLockFail abstractLockFail;

    protected AbstractLock(LockAliasProperties lockAliasProperties, AbstractLockFail abstractLockFail) {
        this.lockAliasProperties = lockAliasProperties;
        this.abstractLockFail = abstractLockFail;
    }

    public Integer getRetryCount() {
        return lockAliasProperties.getRetryCount();
    }

    public Long getWaitTime() {
        return lockAliasProperties.getWaitTime();
    }

    public AbstractLockFail getLockFail() {
        return abstractLockFail;
    }
}
