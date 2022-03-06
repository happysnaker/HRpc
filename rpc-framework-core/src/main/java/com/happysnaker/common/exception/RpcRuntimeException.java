package com.happysnaker.common.exception;

/**
 * 此类指示服务方在运行方法时抛出了异常
 * @author Happysnaker
 * @description
 * @date 2022/3/4
 * @email happysnaker@foxmail.com
 */
public class RpcRuntimeException extends Exception {
    public RpcRuntimeException() {
        super();
    }

    public RpcRuntimeException(String message) {
        super(message);
    }

    public RpcRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcRuntimeException(Throwable cause) {
        super(cause);
    }

    protected RpcRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
