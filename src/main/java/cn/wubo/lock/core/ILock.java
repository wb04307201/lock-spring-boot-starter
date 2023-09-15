package cn.wubo.lock.core;

public interface ILock {

    Boolean tryLock(String key);

    void unLock(String key);
}
