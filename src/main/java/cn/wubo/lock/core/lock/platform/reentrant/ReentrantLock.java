package cn.wubo.lock.core.lock.platform.reentrant;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.lock.platform.AbstractLock;
import cn.wubo.lock.exception.LockRuntimeException;
import lombok.Data;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class ReentrantLock extends AbstractLock {

    private LockClient client;

    public ReentrantLock(LockAliasProperties lockAliasProperties) {
        super(lockAliasProperties);
        this.client = new LockClient();
    }

    @Override
    public Boolean support(String alias) {
        return alias.equals(lockAliasProperties.getAlias());
    }

    @Override
    public Boolean tryLock(String key) {
        return client.tryLock(key);
    }

    @Override
    public Boolean tryLock(String key, Long time, TimeUnit unit) {
        try {
            return client.tryLock(key, time, unit);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // 恢复中断状态
            throw new LockRuntimeException(e);
        }
    }


    @Override
    public void unLock(String key) {
        client.unLock(key);
    }

    @Data
    public class LockClient {

        ConcurrentHashMap<String, Lock> map = new ConcurrentHashMap<>();

        private Lock getLock(String key) {
            map.computeIfAbsent(key, (k) -> new java.util.concurrent.locks.ReentrantLock());
            return map.get(key);
        }

        public Boolean tryLock(String key) {
            return getLock(key).tryLock();
        }

        public Boolean tryLock(String key, Long time, TimeUnit unit) throws InterruptedException {
            return getLock(key).tryLock(time, unit);
        }

        public void unLock(String key) {
            getLock(key).unlock();
        }

    }
}
