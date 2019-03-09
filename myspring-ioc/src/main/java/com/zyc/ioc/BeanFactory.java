package com.zyc.ioc;

/**
 * 基本的IoC容器
 * @author Gordon
 * @since 2018/12/24
 */
public interface BeanFactory {
    Object getBean(String name) throws BeansException;

    <T> T getBean(String name, Class<T> requiredType) throws BeansException;

    Object getBean(String name, Object... args) throws BeansException;

    <T> T getBean(Class<T> requiredType) throws BeansException;

    <T> T getBean(Class<T> requiredType, Object... args) throws BeansException;

    boolean containsBean(String name);

    boolean isSingleton(String name) throws BeansException;

    Class<?> getType(String name) throws BeansException;
}
