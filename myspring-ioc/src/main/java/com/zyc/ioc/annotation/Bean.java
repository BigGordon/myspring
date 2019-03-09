package com.zyc.ioc.annotation;

import java.lang.annotation.*;

/**
 * @author Gordon
 * @since 2019/1/10
 */
@Retention(RetentionPolicy.RUNTIME) //class字节码、运行时可获得注解
@Target(ElementType.TYPE) //用在接口、类、枚举、注解上
@Documented //放入javadoc
@Inherited //可被继承
public @interface Bean {
}
