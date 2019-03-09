package com.zyc.aop;

/**
 * @author Gordon
 * @since 2019/3/6
 */
public class AopException extends RuntimeException {

    public AopException() {}

    public AopException(String msg) {
        super(msg);
    }

    public AopException(Throwable cause) {
        super(cause);
    }
}
