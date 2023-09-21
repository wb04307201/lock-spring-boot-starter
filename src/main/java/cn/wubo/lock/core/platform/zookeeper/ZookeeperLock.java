package cn.wubo.lock.core.platform.zookeeper;

import cn.wubo.lock.core.ILock;
import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.exception.LockRuntimeException;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

import java.util.concurrent.TimeUnit;

public class ZookeeperLock implements ILock {

    private ZookeeperLockRegistry zookeeperLockRegistry;

    public ZookeeperLock(LockAliasProperties lockAliasProperties) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(lockAliasProperties.getZookeeper().getConnect(), new RetryUntilElapsed(lockAliasProperties.getZookeeper().getMaxElapsedTimeMs(), lockAliasProperties.getZookeeper().getSleepMsBetweenRetries()));
        this.zookeeperLockRegistry = new ZookeeperLockRegistry(curatorFramework, "/locks");
    }

    @Override
    public Boolean tryLock(String key) {
        return zookeeperLockRegistry.obtain(key).tryLock();
    }

    @Override
    public Boolean tryLock(String key, long time, TimeUnit unit) {
        try {
            return zookeeperLockRegistry.obtain(key).tryLock(time, unit);
        } catch (InterruptedException e) {
            throw new LockRuntimeException(e.getMessage(), e);
        }
    }

    @Override
    public void unLock(String key) {
        zookeeperLockRegistry.obtain(key).unlock();
    }
}
