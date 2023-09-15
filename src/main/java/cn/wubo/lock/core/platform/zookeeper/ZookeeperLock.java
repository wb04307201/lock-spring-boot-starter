package cn.wubo.lock.core.platform.zookeeper;

import cn.wubo.lock.core.ILock;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryUntilElapsed;
import org.springframework.integration.zookeeper.lock.ZookeeperLockRegistry;

public class ZookeeperLock implements ILock {

    private ZookeeperLockRegistry zookeeperLockRegistry;

    public ZookeeperLock(ZookeeperLockRegistry zookeeperLockRegistry) {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient("127.0.0.1:2181", new RetryUntilElapsed(1000, 4));
        this.zookeeperLockRegistry = new ZookeeperLockRegistry(curatorFramework, "/locks");
    }

    @Override
    public Boolean tryLock(String key) {
        return zookeeperLockRegistry.obtain(key).tryLock();
    }

    @Override
    public void unLock(String key) {
        zookeeperLockRegistry.obtain(key).unlock();
    }
}
