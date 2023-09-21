package cn.wubo.lock.core;

import java.util.concurrent.TimeUnit;

public interface ILock {

    Boolean tryLock(String key);

    Boolean tryLock(String key, long time, TimeUnit unit);

    void unLock(String key);
}
