package com.zyc.aop;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Gordon
 * @since 2019/3/5
 */
public class Pointcut {
    /** 需要进行AOP增强的类 */
    private Class clazz;
    /** 需要进行AOP增强的类里的方法 */
    private List<Method> methods;

    //匹配切口类是否与目标类 名字 相同
    public boolean matches(Class clazz) {
        return this.clazz.equals(clazz);
    }

    public Class getClazz() {
        return clazz;
    }

    public void setClazz(Class clazz) {
        this.clazz = clazz;
    }

    public List<Method> getMethods() {
        return methods;
    }

    public void setMethods(List<Method> methods) {
        this.methods = methods;
    }
}
