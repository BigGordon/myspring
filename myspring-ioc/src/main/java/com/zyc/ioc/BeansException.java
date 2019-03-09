package com.zyc.ioc;

/**
 * @author Gordon
 * @since 2018/12/24
 */
public class BeansException extends RuntimeException {

    public BeansException() {}

    public BeansException(String msg) {
        super(msg);
    }

    public BeansException(Throwable cause) {
        super(cause);
    }
}
