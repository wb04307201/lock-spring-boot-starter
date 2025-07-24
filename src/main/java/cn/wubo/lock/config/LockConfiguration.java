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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
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
        if (lockProperties == null || lockProperties.getProps() == null) {
            throw new IllegalArgumentException("LockProperties or its props list is null.");
        }

        // 锁类型映射表
        Map<String, Function<LockAliasProperties, ILock>> lockFactoryMap = new HashMap<>();
        lockFactoryMap.put("zookeeper", ZookeeperLock::new);
        lockFactoryMap.put("reentrantLock", ReentrantLock::new);
        lockFactoryMap.put("redis", RedissionLock::new);
        lockFactoryMap.put("redis-cluster", RedissionLock::new);
        lockFactoryMap.put("redis-sentinel", RedissionLock::new);

        // @formatter:off
        return lockProperties.getProps().stream()
                .filter(Objects::nonNull)
                .filter(LockAliasProperties::getEnableLock)
                .map(lockAliasProperties -> {
                    String type = lockAliasProperties.getLocktype();
                    if (type == null) {
                        throw new LockRuntimeException("Lock type is null for lock config: " + lockAliasProperties);
                    }
                    Function<LockAliasProperties, ILock> constructor = lockFactoryMap.get(type.toLowerCase());
                    if (constructor == null) {
                        throw new LockRuntimeException("Unsupported lock type: " + type);
                    }
                    return constructor.apply(lockAliasProperties);
                })
                .collect(Collectors.toList());
        // @formatter:on
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
        if (locks == null) {
            throw new IllegalArgumentException("locks parameter cannot be null");
        }
        if (beanFactory == null) {
            throw new IllegalArgumentException("beanFactory parameter cannot be null");
        }
        return new LockAnnotationAspect(locks, beanFactory);
    }


}
