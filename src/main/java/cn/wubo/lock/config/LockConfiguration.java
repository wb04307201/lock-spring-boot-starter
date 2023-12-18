package cn.wubo.lock.config;

import cn.wubo.lock.core.LockAliasProperties;
import cn.wubo.lock.core.aop.LockAnnotationAspect;
import cn.wubo.lock.core.fail.AbstractLockFail;
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
     * 根据LockProperties和lockFails创建锁列表
     *
     * @param lockProperties LockProperties对象
     * @param lockFails      lockFails列表
     * @return 锁列表
     */
    @Bean
    public List<ILock> locks(LockProperties lockProperties, List<AbstractLockFail> lockFails) {
        return lockProperties.getProps().stream().filter(LockAliasProperties::getEnableLock).map(lockAliasProperties -> {
            if ("zookeeper".equals(lockAliasProperties.getLocktype()))
                return new ZookeeperLock(lockAliasProperties, getLockFail(lockFails, lockAliasProperties));
            else if ("reentrantLock".equals(lockAliasProperties.getLocktype()))
                return new ReentrantLock(lockAliasProperties, getLockFail(lockFails, lockAliasProperties));
            else if ("redis".equals(lockAliasProperties.getLocktype()) || "redis-cluster".equals(lockAliasProperties.getLocktype()) || "redis-sentinel".equals(lockAliasProperties.getLocktype()))
                return new RedissionLock(lockAliasProperties, getLockFail(lockFails, lockAliasProperties));
            else throw new LockRuntimeException("不支持的锁类型！");
        }).collect(Collectors.toList());
    }


    /**
     * 根据给定的锁故障列表和锁别名属性，获取匹配的锁故障对象。
     *
     * @param lockFails           锁故障列表
     * @param lockAliasProperties 锁别名属性
     * @return 匹配的锁故障对象，如果没有匹配的锁故障，则返回null
     */
    private AbstractLockFail getLockFail(List<AbstractLockFail> lockFails, LockAliasProperties lockAliasProperties) {
        return lockFails.stream().filter(lockFail -> lockFail.support(lockAliasProperties.getAlias())).findAny().orElse(null);
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
