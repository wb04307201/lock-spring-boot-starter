package cn.wubo.lock.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({LockProperties.class})
public class LockConfiguration {
}
