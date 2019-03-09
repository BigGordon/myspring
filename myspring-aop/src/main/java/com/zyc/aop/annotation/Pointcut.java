package com.zyc.aop.annotation;

import java.lang.annotation.*;

/**
 * @author Gordon
 * @since 2019/3/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@Documented
@Inherited
public @interface Pointcut {
    String targetClass() default "";

    //方法名以逗号隔开
    String targetMethod() default "";

    //是否将所有方法视为目标方法
    boolean allMethods() default false;
}
