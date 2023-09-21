package cn.wubo.lock.core.platform.redission;

import cn.wubo.lock.core.ILock;
import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.exception.LockRuntimeException;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.concurrent.TimeUnit;

public class RedissionLock implements ILock {

    RedissonClient client;

    public RedissionLock(LockAliasProperties lockAliasProperties) {
        Config config = new Config();
        if ("redis".equals(lockAliasProperties.getLocktype())) {
            config.useSingleServer().setAddress(lockAliasProperties.getRedis().getAddress()).setPassword(lockAliasProperties.getRedis().getPassword()).setDatabase(lockAliasProperties.getRedis().getDatabase());
        } else if ("redis-cluster".equals(lockAliasProperties.getLocktype())) {
            config.useClusterServers().addNodeAddress(lockAliasProperties.getRedis().getNodes().toArray(new String[0])).setPassword(lockAliasProperties.getRedis().getPassword());
        } else if ("redis-sentinel".equals(lockAliasProperties.getLocktype())) {
            config.useSentinelServers().addSentinelAddress(lockAliasProperties.getRedis().getNodes().toArray(new String[0])).setPassword(lockAliasProperties.getRedis().getPassword()).setDatabase(lockAliasProperties.getRedis().getDatabase()).setMasterName(lockAliasProperties.getRedis().getMasterName());
        }
        this.client = Redisson.create(config);
    }

    @Override
    public Boolean tryLock(String key) {
        return client.getLock(key).tryLock();
    }

    @Override
    public Boolean tryLock(String key, long time, TimeUnit unit) {
        try {
            return client.getLock(key).tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new LockRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unLock(String key) {
        client.getLock(key).unlock();
    }
}
