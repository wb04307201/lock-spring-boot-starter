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

    /**
     * 获取重试次数
     *
     * @return 重试次数
     */
    public Integer getRetryCount() {
        return lockAliasProperties.getRetryCount();
    }


    /**
     * 获取等待时间
     *
     * @return 返回等待时间
     */
    public Long getWaitTime() {
        return lockAliasProperties.getWaitTime();
    }


    /**
     * 获取失败时的锁对象
     *
     * @return 失败时的锁对象
     */
    public AbstractLockFail getLockFail() {
        return abstractLockFail;
    }

}
