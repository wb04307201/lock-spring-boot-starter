# lock-spring-boot-starter

[![](https://jitpack.io/v/com.gitee.wb04307201/lock-spring-boot-starter.svg)](https://jitpack.io/#com.gitee.wb04307201/lock-spring-boot-starter)

> 这是一个锁适配器  
> 可配置多个redis锁和zookeeper锁  
> 通过注解@Locking对方法加锁  
> 注解支持根据SpEL表达式获取加锁key

## [代码示例](https://gitee.com/wb04307201/lock-demo)

## 第一步 增加 JitPack 仓库

```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```

## 第二步 引入jar

```xml
<dependency>
    <groupId>com.gitee.wb04307201</groupId>
    <artifactId>lock-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## 第三步 在启动类上加上`@EnableLock`注解

```java
@EnableLock
@SpringBootApplication
public class LockDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(LockDemoApplication.class, args);
    }

}
```

## 第四步 `application.yml`配置文件中添加以下相关配置，可以配置多个缓存然后形成多级缓存使用

```yaml
lock:
  props:
      # 别名
    - alias: redis-1
      # 单例redis
      locktype: redis
      redis:
        address: redis://127.0.0.1:6379
        password: mypassword
        # 数据库，默认是0
        database: 0
      # 别名
    - alias: redis-2
      # 单例redis集群
      locktype: redis-cluster
      redis:
        password: mypassword
        # 集群节点
        nodes: 
          - redis://127.0.0.1:6379
          - redis://127.0.0.1:6379
          - redis://127.0.0.1:6379
      # 别名
    - alias: redis-3
      # 单例redis哨兵
      locktype: redis-sentinel
      redis:
        password: mypassword
        # 数据库，默认是0
        database: 0
        # 集群节点
        nodes:
          - redis://127.0.0.1:6379
          - redis://127.0.0.1:6379
          - redis://127.0.0.1:6379
        # 主服务名
        masterName: masterName

# debug日志
logging:
  level:
    cn:
      wubo:
        lock: debug
```

## 第五步 通过注解使用缓存

```java
@Component
public class DemoService {

    @Locking(alias = "test-1", keys = "#key")
    public String doWork(String key) {
        try {
            Thread.currentThread().wait(new Random(30).nextLong() * 1000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return key;
    }
}
```

> keys支持SpEL表达式，#匹配参数，@匹配上下文
> 如果需要设置超时时间，请配置Locking注解time和unit属性


## 待办

- [ ] *增加使用数据库加锁*