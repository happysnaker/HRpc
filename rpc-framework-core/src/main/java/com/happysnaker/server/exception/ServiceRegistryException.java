package com.happysnaker.server.exception;

/**
 * 注册服务失败是抛出的异常
 * @author Happysnaker
 * @description
 * @date 2022/3/2
 * @email happysnaker@foxmail.com
 */
public class ServiceRegistryException extends Exception {
    public ServiceRegistryException() {
        super();
    }

    public ServiceRegistryException(String message) {
        super(message);
    }

    public ServiceRegistryException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceRegistryException(Throwable cause) {
        super(cause);
    }

    protected ServiceRegistryException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
