package cn.wubo.lock.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "lock")
public class LockProperties {
    // database redission zookeeper
    private String locktype;
}
