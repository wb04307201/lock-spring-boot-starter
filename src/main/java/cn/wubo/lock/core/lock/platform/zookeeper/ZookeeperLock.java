package cn.wubo.lock.core.lock.platform.zookeeper;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.lock.platform.AbstractLock;
import cn.wubo.lock.exception.LockRuntimeException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

import java.util.concurrent.TimeUnit;

public class ZookeeperLock extends AbstractLock {

    private ZookeeperLockRegistry zookeeperLockRegistry;

    public ZookeeperLock(LockAliasProperties lockAliasProperties) {
        super(lockAliasProperties);
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(lockAliasProperties.getZookeeper().getConnect(), new RetryUntilElapsed(lockAliasProperties.getZookeeper().getMaxElapsedTimeMs(), lockAliasProperties.getZookeeper().getSleepMsBetweenRetries()));
        curatorFramework.start();
        this.zookeeperLockRegistry = new ZookeeperLockRegistry(curatorFramework, "/locks");
    }

    @Override
    public Boolean support(String alias) {
        return alias.equals(lockAliasProperties.getAlias());
    }

    @Override
    public Boolean tryLock(String key) {
        return zookeeperLockRegistry.obtain(key).tryLock();
    }

    @Override
public Boolean tryLock(String key, Long time, TimeUnit unit) {
    try {
        return zookeeperLockRegistry.obtain(key).tryLock(time, unit);
    } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new LockRuntimeException(e);
    }
}


    @Override
    public void unLock(String key) {
        zookeeperLockRegistry.obtain(key).unlock();
    }
}
