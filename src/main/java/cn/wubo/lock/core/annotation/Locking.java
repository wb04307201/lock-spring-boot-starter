package cn.wubo.lock.core.annotation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Locking {
    /**
     * 配置的别名
     */
    String alias();

    /**
     * support SPEL expresion
     * 锁的key = alias + keys
     */
    String[] keys() default "";

    long time() default -1;

    TimeUnit unit() default TimeUnit.SECONDS;
}
