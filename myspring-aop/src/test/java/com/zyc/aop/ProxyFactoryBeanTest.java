package com.zyc.aop;

import com.zyc.aop.classesForTest.TargetClass;
import com.zyc.ioc.ApplicationContext;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.CoreMatchers.containsString;

/**
 * @author Gordon
 * @since 2019/3/8
 */
public class ProxyFactoryBeanTest {

    private ApplicationContext applicationContext;

    @Before
    public void init() {
        this.applicationContext = new ApplicationContext();
        this.applicationContext.setPackageToScan(this.getClass().getPackage().getName());
    }

    @Test
    public void generalAopWithAnnotation() {
        this.applicationContext.refresh();
        //把标准输出定向至ByteArrayOutputStream中
        final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));

        TargetClass targetClass = (TargetClass)this.applicationContext.getBean("com.zyc.aop.classesForTest.TargetClass");
        targetClass.targetMethodA();

        Assert.assertThat(outContent.toString(), containsString("targetMethodA"));
        Assert.assertThat(outContent.toString(), containsString("Advice"));
    }
}
