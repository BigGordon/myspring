package com.zyc.aop.annotation;

import java.lang.annotation.*;

/**
 * @author Gordon
 * @since 2019/3/6
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Inherited
public @interface Aspect{
}
