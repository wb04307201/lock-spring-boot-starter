package cn.wubo.lock.core.aop;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface Locking {
    String alias();

    String key();

    long time();

    TimeUnit unit();
}
