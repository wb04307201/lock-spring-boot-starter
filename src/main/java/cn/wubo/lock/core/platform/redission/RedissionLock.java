package cn.wubo.lock.core.platform.redission;

import cn.wubo.lock.core.ILock;
import cn.wubo.lock.core.LockAliasProperties;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.ArrayList;
import java.util.List;

public class RedissionLock implements ILock {

    RedissonClient client;

    public RedissionLock(LockAliasProperties lockAliasProperties) {
        this.client = client;
    }

    public void aaa() {
        //创建配置
        Config config = new Config();
        List<String> newNodeList = new ArrayList<>();
        String[] nodeArray = nodes.split(",");

        for (String oneNode : nodeArray) {
            newNodeList.add("redis://" + oneNode);
        }

        String[] addressArray = newNodeList.toArray(new String[newNodeList.size()]);

        //集群模式,集群节点的地址须使用“redis://”前缀，否则将会报错。
        //此例集群为3节点，各节点1主1从
        config.useClusterServers().addNodeAddress(addressArray);
        config.useClusterServers().setPassword(password);
        config.useClusterServers().setTimeout(timeout);

        config.useSingleServer().setAddress();
        config.useSingleServer().setDatabase();
        config.useSingleServer().setPassword();

        config.useSentinelServers().setSentinelAddresses();

        //创建客户端(发现这一非常耗时，基本在2秒-4秒左右)
        RedissonClient redisson = Redisson.create(config);
    }

    @Override
    public Boolean tryLock(String key) {
        return client.getLock(key).tryLock();
    }

    @Override
    public void unLock(String key) {
        client.getLock(key).unlock();
    }
}
