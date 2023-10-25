package cn.wubo.lock.core.fail;

public abstract class AbstractLockFail {

    protected String alias;

    protected AbstractLockFail(String alias) {
        this.alias = alias;
    }

    public Boolean support(String alias) {
        return alias.equals(this.alias);
    }

    public abstract void fail(Object... objs);

}
