package cn.wubo.lock;

import cn.wubo.lock.config.LockConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({LockConfiguration.class})
public @interface EnableLock {
}
