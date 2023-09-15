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
        private String host = "localhost";
        // 单例端口路
        private Integer port = 6379;
        // 密码
        private String password;
        // 数据库
        private Integer database = 0;

        // 连接池最大连接数（使用负值表示没有限制）
        private int maxTotal = 8;
        // 连接池中的最大空闲连接
        private int maxIdle = 8;
        // 连接池中的最小空闲连接
        private int minIdle = 0;
        // 连接池最大阻塞等待时间(使用负值表示没有限制) 默认为-1
        private Long maxWait = -1L;
        //连接超时的时间
        private Integer timeout = 2000;
        //集群
        private ClusterProperties cluster;
        //哨兵
        private SentinelProperties sentinel;

        @Data
        public class ClusterProperties {
            // 集群节点，必输
            private List<String> nodes;
            // 出现异常最大重试次数
            private Integer maxAttempts = 5;
        }

        @Data
        public class SentinelProperties {
            // 烧饼节点，必输
            private List<String> nodes;
            // 主节点名称，默认为空，必输
            private String masterName;
            // 用户，默认为空
            private String user;
        }
    }

    @Data
    public class Zookeeper {

    }
}
