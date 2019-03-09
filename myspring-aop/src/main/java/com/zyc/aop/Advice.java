package com.zyc.aop;

import java.lang.reflect.Method;

/**
 * @author Gordon
 * @since 2019/3/5
 */
public class Advice {
    /** AOP增强方法所在的类 */
    private Class clazz;
    /** AOP增强方法 */
    private Method method;

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }
}
