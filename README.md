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
    <version>1.0.2</version>
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

## 第四步 `application.yml`配置文件中添加相关配置

```yaml
lock:
  props:
      # 别名
    - alias: redis-1
      # 单例redis
      locktype: redis
      # 启用锁，默认true
      enableLock: true
      # 加锁失败重试次数，默认0不重试，-1一直重试
      retryCount: 0
      # 重试等待时间，默认3000，单位毫秒
      waitTime: 3000
      redis:
        address: redis://ip:port
        password: mypassword
        # 数据库，默认是0
        database: 0
      # 别名
    - alias: redis-2
      # 单例redis集群
      locktype: redis-cluster
      # 启用锁，默认true
      enableLock: true
      # 加锁失败重试次数，默认0不重试，-1一直重试
      retryCount: 0
      # 重试等待时间，默认3000，单位毫秒
      waitTime: 3000
      redis:
        password: mypassword
        # 集群节点
        nodes:
          - redis://ip:port
          - redis://ip:port
          - redis://ip:port
      # 别名
    - alias: redis-3
      # 单例redis哨兵
      locktype: redis-sentinel
      # 启用锁，默认true
      enableLock: true
      # 加锁失败重试次数，默认0不重试，-1一直重试
      retryCount: 0
      # 重试等待时间，默认3000，单位毫秒
      waitTime: 3000
      redis:
        password: mypassword
        # 数据库，默认0
        database: 0
        # 集群节点
        nodes:
          - redis://ip:port
          - redis://ip:port
          - redis://ip:port
        # 主服务名
        masterName: masterName
      # 别名
    - alias: zookeeper-1
      # zookeeper
      locktype: zookeeper
      # 启用锁，默认true
      enableLock: true
      # 加锁失败重试次数，默认0不重试，-1一直重试
      retryCount: 0
      # 重试等待时间，默认3000，单位毫秒
      waitTime: 3000
      zookeeper:
        # zookeeper服务地址
        connect: ip:port,ip:port...
        # 最大重试时间，默认值1000
        maxElapsedTimeMs: 1000
        # 每次重试的间隔时间，默认值4
        sleepMsBetweenRetries: 4
        # 锁目录
        root: /locks
      # 别名
    - alias: reentrantLock-1
      # ReentrantLock,可重入锁，只在服务内部加锁
      locktype: reentrantLock
      # 启用锁，默认true
      enableLock: true
      # 加锁失败重试次数，默认0不重试，-1一直重试
      retryCount: 0
      # 重试等待时间，默认3000，单位毫秒
      waitTime: 3000

# debug日志
logging:
  level:
    cn:
      wubo:
        lock: debug
```

## 第五步 通过注解使用锁

```java
@Component
public class DemoService {

    @Locking(alias = "test-1", keys = "#key")
    public String doWork1(String key) {
        try {
            Double time = (Math.random() * 30 + 1) * 1000;
            log.info(Thread.currentThread().getId() + " " + time);
            Thread.currentThread().wait(time.longValue());
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
        return key;
    }
}
```

> keys支持SpEL表达式，#匹配参数，@匹配上下文
> 如果需要设置超时时间，请配置Locking注解time和unit属性


## 其他1：获取锁失败处理
```java
@Slf4j
@Component
public class Test3LockFail extends AbstractLockFail {
    public Test3LockFail() {
        // 和锁配置中的别名一致
        super("test-3");
    }

    @Override
    public void fail(Object... args) {
        log.info(Thread.currentThread().getId() + " 失败了！ 参数--》{}",Arrays.toString(args));
    }
}
```

下面是重试3次，重试等待3秒，加锁失败调用方法的运行日志
```bash
2023-10-26 09:19:16.581 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:58 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [nPool-worker-11] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:57 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-8] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:62 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [nPool-worker-10] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:64 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:61 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.581 DEBUG 108896 --- [nPool-worker-15] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:60 methoddoWork3 alias:test-3 key:test-3:test_key_001 尝试加锁
2023-10-26 09:19:16.586 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.585 DEBUG 108896 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:58 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [nPool-worker-11] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:57 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [onPool-worker-8] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:62 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [nPool-worker-15] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:60 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.585 DEBUG 108896 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:61 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:16.586 DEBUG 108896 --- [nPool-worker-10] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:64 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.586 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败
2023-10-26 09:19:16.594  INFO 108896 --- [onPool-worker-6] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：61 time:5972.057289984968
2023-10-26 09:19:16.595 DEBUG 108896 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:61 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
2023-10-26 09:19:19.588 DEBUG 108896 --- [nPool-worker-11] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:57 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [nPool-worker-15] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:60 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:58 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [onPool-worker-8] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:62 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [nPool-worker-10] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:64 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.588 DEBUG 108896 --- [nPool-worker-11] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:57 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:19.588 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第1次重试
2023-10-26 09:19:19.589  INFO 108896 --- [nPool-worker-11] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：57 time:2308.4955182796143
2023-10-26 09:19:19.589 DEBUG 108896 --- [nPool-worker-11] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:57 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
2023-10-26 09:19:22.588 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588 DEBUG 108896 --- [nPool-worker-15] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:60 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:22.588 DEBUG 108896 --- [onPool-worker-8] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:62 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588 DEBUG 108896 --- [nPool-worker-10] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:64 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588 DEBUG 108896 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:58 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.588  INFO 108896 --- [nPool-worker-13] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：59 time:7854.4647680142525
2023-10-26 09:19:22.589 DEBUG 108896 --- [nPool-worker-13] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:59 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
2023-10-26 09:19:22.604 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.604 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第2次重试
2023-10-26 09:19:22.604 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:22.604  INFO 108896 --- [onPool-worker-1] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：63 time:12838.747332898187
2023-10-26 09:19:22.604 DEBUG 108896 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:63 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
2023-10-26 09:19:25.601 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.601 DEBUG 108896 --- [nPool-worker-15] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:60 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.601 DEBUG 108896 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:58 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.601 DEBUG 108896 --- [nPool-worker-10] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:64 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.601 DEBUG 108896 --- [onPool-worker-8] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:62 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.601 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:25.601  INFO 108896 --- [onPool-worker-9] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：55 time:25678.06024063111
2023-10-26 09:19:25.601  INFO 108896 --- [nPool-worker-15] cn.wubo.lock.Test3LockFail               : Test3LockFail fail thread：60 args:[test_key_001]
2023-10-26 09:19:25.601  INFO 108896 --- [onPool-worker-4] cn.wubo.lock.Test3LockFail               : Test3LockFail fail thread：58 args:[test_key_001]
2023-10-26 09:19:25.601  INFO 108896 --- [nPool-worker-10] cn.wubo.lock.Test3LockFail               : Test3LockFail fail thread：64 args:[test_key_001]
2023-10-26 09:19:25.601  INFO 108896 --- [onPool-worker-8] cn.wubo.lock.Test3LockFail               : Test3LockFail fail thread：62 args:[test_key_001]
2023-10-26 09:19:25.602 DEBUG 108896 --- [onPool-worker-9] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:55 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
2023-10-26 09:19:25.617 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁失败 第3次重试
2023-10-26 09:19:25.617 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 加锁
2023-10-26 09:19:25.617  INFO 108896 --- [onPool-worker-2] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：56 time:15733.156151486939
2023-10-26 09:19:25.617 DEBUG 108896 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:56 methoddoWork3 alias:test-3 key:test-3:test_key_001 解锁
```


## 待办

- [ ] *增加使用数据库加锁*