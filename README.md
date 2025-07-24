# lock-spring-boot-starter

[![](https://jitpack.io/v/com.gitee.wb04307201/lock-spring-boot-starter.svg)](https://jitpack.io/#com.gitee.wb04307201/lock-spring-boot-starter)
[![star](https://gitee.com/wb04307201/lock-spring-boot-starter/badge/star.svg?theme=dark)](https://gitee.com/wb04307201/lock-spring-boot-starter)
[![fork](https://gitee.com/wb04307201/lock-spring-boot-starter/badge/fork.svg?theme=dark)](https://gitee.com/wb04307201/lock-spring-boot-starter)
[![star](https://img.shields.io/github/stars/wb04307201/lock-spring-boot-starter)](https://github.com/wb04307201/lock-spring-boot-starter)
[![fork](https://img.shields.io/github/forks/wb04307201/lock-spring-boot-starter)](https://github.com/wb04307201/lock-spring-boot-starter)  
![MIT](https://img.shields.io/badge/License-Apache2.0-blue.svg) ![JDK](https://img.shields.io/badge/JDK-17+-green.svg) ![SpringBoot](https://img.shields.io/badge/Srping%20Boot-3+-green.svg)

> 一个注解@Locking搞定锁
> 通过或配置可切换分布式redis锁、zookeeper锁，以及单节点ReentrantLock锁
> 注解@Locking支持根据SpEL表达式对加锁的维度进一步细化

## 代码示例
1. 使用[锁注解](https://gitee.com/wb04307201/lock-spring-boot-starter)实现的[锁注解Demo](https://gitee.com/wb04307201/lock-demo)

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
1.1.0版本后升级到jdk17 SpringBoot3+
```xml
<dependency>
    <groupId>com.gitee.wb04307201</groupId>
    <artifactId>lock-spring-boot-starter</artifactId>
    <version>1.1.3</version>
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

## 第四步 在`application.yml`配置文件中按需添加相关配置
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

## 可通过配置日志及级别增加锁相关debug日志输出
```yaml
logging:
  level:
    cn:
      wubo:
        lock: debug
```

下面是通过application.yml配置重试3次，重试等待3秒时的输出的debug日志
```bash
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-3] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:48 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:47 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-7] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:52 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:49 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.172 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.175 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:16.188  INFO 12188 --- [onPool-worker-5] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：50 time:27501.381703194307
2025-07-10 17:04:16.189 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:16.191 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.191 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:16.192  INFO 12188 --- [onPool-worker-5] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：50 time:28339.382065754926
2025-07-10 17:04:16.192 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:16.193 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.193 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:16.193  INFO 12188 --- [onPool-worker-5] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：50 time:13877.48878494065
2025-07-10 17:04:16.194 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:16.194 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 尝试加锁
2025-07-10 17:04:16.195 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:16.195  INFO 12188 --- [onPool-worker-5] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：50 time:10239.0796779004
2025-07-10 17:04:16.195 DEBUG 12188 --- [onPool-worker-5] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:50 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:19.187 DEBUG 12188 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:47 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.187 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.187 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.187 DEBUG 12188 --- [onPool-worker-3] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:48 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.187 DEBUG 12188 --- [onPool-worker-7] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:52 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.188 DEBUG 12188 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:49 methoddoWork3 alias:test-3 key:test-3 加锁失败 第1次重试
2025-07-10 17:04:19.188 DEBUG 12188 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:47 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:19.189  INFO 12188 --- [onPool-worker-2] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：47 time:30232.181660814866
2025-07-10 17:04:19.191 DEBUG 12188 --- [onPool-worker-2] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:47 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-7] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:52 methoddoWork3 alias:test-3 key:test-3 加锁失败 第2次重试
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-3] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:48 methoddoWork3 alias:test-3 key:test-3 加锁失败 第2次重试
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 加锁失败 第2次重试
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 加锁失败 第2次重试
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-7] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:52 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:22.201 DEBUG 12188 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:49 methoddoWork3 alias:test-3 key:test-3 加锁失败 第2次重试
2025-07-10 17:04:22.201  INFO 12188 --- [onPool-worker-7] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：52 time:19611.613933892113
2025-07-10 17:04:22.202 DEBUG 12188 --- [onPool-worker-7] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:52 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:25.203 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 加锁失败 第3次重试
2025-07-10 17:04:25.203 DEBUG 12188 --- [onPool-worker-3] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:48 methoddoWork3 alias:test-3 key:test-3 加锁失败 第3次重试
2025-07-10 17:04:25.203 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:25.203 DEBUG 12188 --- [onPool-worker-3] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:48 methoddoWork3 alias:test-3 key:test-3 加锁失败
2025-07-10 17:04:25.203  INFO 12188 --- [onPool-worker-6] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：51 time:3412.527522089528
2025-07-10 17:04:25.203  INFO 12188 --- [onPool-worker-3] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：48 time:6984.674568104761
2025-07-10 17:04:25.203 DEBUG 12188 --- [onPool-worker-6] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:51 methoddoWork3 alias:test-3 key:test-3 解锁
2025-07-10 17:04:25.213 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 加锁失败 第3次重试
2025-07-10 17:04:25.213 DEBUG 12188 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:49 methoddoWork3 alias:test-3 key:test-3 加锁失败 第3次重试
2025-07-10 17:04:25.213 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 加锁成功
2025-07-10 17:04:25.213 DEBUG 12188 --- [onPool-worker-4] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:49 methoddoWork3 alias:test-3 key:test-3 加锁失败
2025-07-10 17:04:25.213  INFO 12188 --- [onPool-worker-4] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：49 time:15432.098826015983
2025-07-10 17:04:25.213  INFO 12188 --- [onPool-worker-1] cn.wubo.lock.DemoService                 : DemoService doWork3 thread：46 time:15086.689457518112
2025-07-10 17:04:25.214 DEBUG 12188 --- [onPool-worker-1] c.w.lock.core.aop.LockAnnotationAspect   : LockAnnotationAspect thread:46 methoddoWork3 alias:test-3 key:test-3 解锁
```
