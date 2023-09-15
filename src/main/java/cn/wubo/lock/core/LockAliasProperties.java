package cn.wubo.lock.core;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@Data
public class LockAliasProperties {
    /**
     * 别名
     */
    private String aliasName;

    // database redis redis-cluster redis-sentinel zookeeper
    private String locktype;

    private Database database = new Database();

    private RedisProperties redis = new RedisProperties();

    private Zookeeper zookeeper = new Zookeeper();

    @Data
    public class Database {
        private String diverClassName;
        private String url;
        private String username;
        private String password;
    }

    @Data
    public class RedisProperties {
        // 单例地址
        private String address = "localhost:6379";
        // 密码
        private String password;
        // 数据库
        private Integer database = 0;
        // 集群、哨兵节点
        private List<String> nodes;
        // 烧饼主节点名
        private String masterName;
    }

    @Data
    public class Zookeeper {
        // list of servers to connect to ip:port,ip:port...
        private String connect;
        // maxElapsedTimeMs 最大重试时间
        private Integer maxElapsedTimeMs;
        // sleepMsBetweenRetries 每次重试的间隔时间
        private Integer sleepMsBetweenRetries;
    }
}
