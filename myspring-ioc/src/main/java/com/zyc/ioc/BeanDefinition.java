package com.zyc.ioc;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Map;

/**
 * Bean的基本信息
 * @author Gordon
 * @since 2018/12/26
 */
public class BeanDefinition {

    /** 是否是单例模式 */
    protected boolean singleton;

    /** 该Bean的类 */
    protected Class<?> clazz;

    /** 该Bean的构造器 */
    protected Constructor<?> constructor;

    /** 该Bean的属性，需要装配其他Bean */
    protected Map<Field, String> properties;

    /** 持有该Bean的IOC容器 */
    protected ApplicationContext applicationContext;


    public boolean isSingleton() {
        return singleton;
    }

    public void setSingleton(boolean singleton) {
        this.singleton = singleton;
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public void setClazz(Class<?> clazz) {
        this.clazz = clazz;
    }

    public Constructor<?> getConstructor() {
        return constructor;
    }

    public void setConstructor(Constructor<?> constructor) {
        this.constructor = constructor;
    }

    public Map<Field, String> getProperties() {
        return properties;
    }

    public void setProperties(Map<Field, String> properties) {
        this.properties = properties;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }
}
