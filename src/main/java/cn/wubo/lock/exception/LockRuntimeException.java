package cn.wubo.lock.exception;

public class LockRuntimeException extends RuntimeException {

    public LockRuntimeException(String message) {
        super(message);
    }

    public LockRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }
}
