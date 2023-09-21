package cn.wubo.lock.core.lock;

import java.util.concurrent.TimeUnit;

public interface ILock {

    Boolean support(String alias);

    Boolean tryLock(String key);

    Boolean tryLock(String key, Long time, TimeUnit unit);

    void unLock(String key);
}
