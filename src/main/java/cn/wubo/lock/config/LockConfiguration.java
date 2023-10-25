package cn.wubo.lock.config;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.aop.LockAnnotationAspect;
import cn.wubo.lock.core.lock.ILock;
import cn.wubo.lock.core.lock.platform.redission.RedissionLock;
import cn.wubo.lock.core.lock.platform.reentrant.ReentrantLock;
import cn.wubo.lock.core.lock.platform.zookeeper.ZookeeperLock;
import cn.wubo.lock.exception.LockRuntimeException;
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
        return lockProperties.getProps().stream().filter(LockAliasProperties::getEnableLock).map(lockAliasProperties -> {
            if ("zookeeper".equals(lockAliasProperties.getLocktype())) return new ZookeeperLock(lockAliasProperties);
            else if ("reentrantLock".equals(lockAliasProperties.getLocktype()))
                return new ReentrantLock(lockAliasProperties);
            else if ("redis".equals(lockAliasProperties.getLocktype()) || "redis-cluster".equals(lockAliasProperties.getLocktype()) || "redis-sentinel".equals(lockAliasProperties.getLocktype()))
                return new RedissionLock(lockAliasProperties);
            else throw new LockRuntimeException("不支持的锁类型！");
        }).collect(Collectors.toList());
    }

    @Bean
    public LockAnnotationAspect lockAspect(List<ILock> locks, BeanFactory beanFactory) {
        return new LockAnnotationAspect(locks, beanFactory);
    }
}
