package cn.wubo.lock.core;

import lombok.Data;

import java.util.List;

@Data
public class LockAliasProperties {
    /**
     * 别名
     */
    private String alias;

    // redis redis-cluster redis-sentinel zookeeper reentrantLock
    private String locktype;

    private RedisProperties redis = new RedisProperties();

    private Zookeeper zookeeper = new Zookeeper();

    private Boolean enableLock = true;

    private Integer retryCount = 0;

    private Long waittime = 3 * 1000L;

    @Data
    public static class RedisProperties {
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
    public static class Zookeeper {
        // list of servers to connect to ip:port,ip:port...
        private String connect;
        // maxElapsedTimeMs 最大重试时间
        private Integer maxElapsedTimeMs = 1000;
        // sleepMsBetweenRetries 每次重试的间隔时间
        private Integer sleepMsBetweenRetries = 4;
        // root 锁目录
        private String root = "/locks";
    }
}
