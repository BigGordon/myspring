package com.zyc.aop.classesForTest;

import com.zyc.aop.annotation.Advice;
import com.zyc.aop.annotation.Aspect;
import com.zyc.aop.annotation.Pointcut;
import com.zyc.ioc.annotation.Bean;

/**
 * @author Gordon
 * @since 2019/3/8
 */
@Aspect
@Bean
public class GeneralAdvisor {

    @Pointcut(targetClass = "com.zyc.aop.classesForTest.TargetClass", targetMethod = "targetMethodA")
    public void pointcut() {}

    @Advice
    public void advice() {
        System.out.println("Advice");
    }
}
