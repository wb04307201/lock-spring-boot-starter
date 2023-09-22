package cn.wubo.lock.config;

import cn.wubo.lock.core.aop.LockAnnotationAspect;
import cn.wubo.lock.core.lock.ILock;
import cn.wubo.lock.core.lock.platform.redission.RedissionLock;
import cn.wubo.lock.core.lock.platform.zookeeper.ZookeeperLock;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@EnableConfigurationProperties({LockProperties.class})
public class LockConfiguration {

    LockProperties lockProperties;

    public LockConfiguration(LockProperties lockProperties) {
        this.lockProperties = lockProperties;
    }

    @Bean
    public List<ILock> locks(LockProperties lockProperties) {
        return lockProperties.getConfig().stream().map(lockAliasProperties -> {
            if ("zookeeper".equals(lockAliasProperties.getLocktype())) return new ZookeeperLock(lockAliasProperties);
            else return new RedissionLock(lockAliasProperties);
        }).collect(Collectors.toList());
    }

    @Bean
    public LockAnnotationAspect lockAspect(List<ILock> locks, BeanFactory beanFactory) {
        return new LockAnnotationAspect(locks, beanFactory);
    }
}
