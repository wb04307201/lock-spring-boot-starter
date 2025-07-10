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

    /**
     * 根据锁属性配置创建并初始化锁实例
     * 该方法通过读取配置文件中锁的属性，动态创建锁对象列表
     * 支持多种锁类型的创建，包括Zookeeper锁、ReentrantLock锁和Redis锁等
     *
     * @param lockProperties 锁属性配置对象，包含锁的配置信息
     * @return 返回一个锁对象列表，列表中的锁类型根据配置动态生成
     */
    @Bean
    public List<ILock> locks(LockProperties lockProperties) {
        // 根据锁属性配置，过滤出需要启用的锁，并根据锁类型创建对应的锁实例
        return lockProperties.getProps().stream().filter(LockAliasProperties::getEnableLock).map(lockAliasProperties -> {
            // 根据不同的锁类型创建相应的锁实例
            if ("zookeeper".equals(lockAliasProperties.getLocktype()))
                return new ZookeeperLock(lockAliasProperties);
            else if ("reentrantLock".equals(lockAliasProperties.getLocktype()))
                return new ReentrantLock(lockAliasProperties);
            else if ("redis".equals(lockAliasProperties.getLocktype()) || "redis-cluster".equals(lockAliasProperties.getLocktype()) || "redis-sentinel".equals(lockAliasProperties.getLocktype()))
                return new RedissionLock(lockAliasProperties);
            else throw new LockRuntimeException("不支持的锁类型！");
        }).collect(Collectors.toList());
    }

    /**
     * 创建LockAnnotationAspect的Bean
     *
     * @param locks       锁集合
     * @param beanFactory Bean工厂
     * @return LockAnnotationAspect的Bean实例
     */
    @Bean
    public LockAnnotationAspect lockAspect(List<ILock> locks, BeanFactory beanFactory) {
        return new LockAnnotationAspect(locks, beanFactory);
    }

}
