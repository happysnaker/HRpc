package com.happysnaker.common.exception;

/**
 * 指示无法更新中央服务列表
 * @author Happysnaker
 * @description
 * @date 2022/3/1
 * @email happysnaker@foxmail.com
 */
public class CanNotUpdateServiceListException extends Exception {
    public CanNotUpdateServiceListException() {
        super();
    }

    public CanNotUpdateServiceListException(String message) {
        super(message);
    }

    public CanNotUpdateServiceListException(String message, Throwable cause) {
        super(message, cause);
    }

    public CanNotUpdateServiceListException(Throwable cause) {
        super(cause);
    }

    protected CanNotUpdateServiceListException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
