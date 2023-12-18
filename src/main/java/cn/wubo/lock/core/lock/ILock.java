package cn.wubo.lock.core.lock;

import cn.wubo.lock.core.fail.AbstractLockFail;

import java.util.concurrent.TimeUnit;

public interface ILock {

    Boolean support(String alias);

    /**
     * 尝试对给定的键进行加锁操作。
     *
     * @param key 锁的键值
     * @return 如果成功加锁返回true，否则返回false
     */
    Boolean tryLock(String key);

    /**
     * 尝试对给定的键加锁，设置最大等待时间和时间单位
     *
     * @param key  锁的键值
     * @param time 最大等待时间
     * @param unit 时间单位
     * @return 如果成功加锁返回true，否则返回false
     */
    Boolean tryLock(String key, Long time, TimeUnit unit);

    /**
     * 重写的方法，用于解锁指定的键
     *
     * @param key 锁的键值
     */
    void unLock(String key);

    Integer getRetryCount();

    Long getWaitTime();

    AbstractLockFail getLockFail();
}
