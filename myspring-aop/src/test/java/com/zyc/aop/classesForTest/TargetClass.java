package com.zyc.aop.classesForTest;

import com.zyc.ioc.annotation.Bean;

/**
 * @author Gordon
 * @since 2019/3/8
 */
@Bean
public class TargetClass {

    public void targetMethodA() {
        System.out.println("targetMethodA");
    }

    public void targetMethodB() {
        System.out.println("targetMethodB");
    }
}
