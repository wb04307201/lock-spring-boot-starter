package cn.wubo.lock.core.lock.platform;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.lock.ILock;

public abstract class AbstractLock implements ILock {

    protected LockAliasProperties lockAliasProperties;

    protected AbstractLock(LockAliasProperties lockAliasProperties) {
        this.lockAliasProperties = lockAliasProperties;
    }

    public Integer getRetryCount() {
        return lockAliasProperties.getRetryCount();
    }

    public Long getWaitTime() {
        return lockAliasProperties.getWaitTime();
    }

}
