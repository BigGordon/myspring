package com.zyc.ioc;

/**
 * @author Gordon
 * @since 2019/3/1
 */
public interface FactoryBean {
    /** 由Bean工厂来生成Bean */
    Object createBean(BeanDefinition beanDefinition);
}
