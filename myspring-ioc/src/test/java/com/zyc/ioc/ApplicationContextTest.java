package com.zyc.ioc;

import com.zyc.ioc.annotation.Autowired;
import com.zyc.ioc.annotation.Bean;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Gordon
 * @since 2019/1/7
 */
public class ApplicationContextTest {

    private ApplicationContext applicationContext;

    /* 用来进行测试的内部类 */
    //一般类
    public static class DevTestBean {
        public DevTestBean() { }

        @Override
        public String toString() {
            return "DevTestBean";
        }
    }

    //测试@Bean注解
    @Bean
    public static class BeanAnnotation {
        @Override
        public String toString() {
            return "BeanAnnotation";
        }
    }

    //测试@Autowired注解
    @Bean
    public static class AutowiredAnnotation {

        @Autowired
        DependentBean dependentBean;

        @Override
        public String toString() {
            return "AutowiredAnnotation";
        }
    }

    @Bean
    public static class DependentBean {
        @Override
        public String toString() {
            return "DependentBean";
        }
    }

    //循环依赖类A
    @Bean
    public static class CircularDependencyA {

        @Autowired
        CircularDependencyB circularDependencyB;

        @Override
        public String toString() {
            return "CircularDependencyA";
        }
    }

    //循环依赖类B
    @Bean
    public static class CircularDependencyB {

        @Autowired
        CircularDependencyA circularDependencyA;

        @Override
        public String toString() {
            return "CircularDependencyB";
        }
    }

    @Before
    public void init() {
        this.applicationContext = new ApplicationContext();
        this.applicationContext.setPackageToScan(this.getClass().getPackage().getName());
    }

    @Test
    public void getBean() {
        //手动添加测试Bean的BeanDefinition
        BeanDefinition devBD = new BeanDefinition();
        devBD.setSingleton(true);
        devBD.setClazz(DevTestBean.class);
        try {
            devBD.setConstructor(DevTestBean.class.getConstructor());
        } catch (Exception e) {
            e.printStackTrace();
        }

        applicationContext.getBeanDefinitions().put("DevTest", devBD);

        DevTestBean bean = (DevTestBean) this.applicationContext.getBean("DevTest");
        Assert.assertEquals(bean.toString(), "DevTestBean");
    }

    @Test
    public void beanAnnotation() {
        this.applicationContext.refresh();
        BeanAnnotation bean = (BeanAnnotation) this.applicationContext.getBean(BeanAnnotation.class.getName());
        Assert.assertEquals(bean.toString(), "BeanAnnotation");
    }

    @Test
    public void autowiredAnnotation() {
        this.applicationContext.refresh();
        AutowiredAnnotation bean = (AutowiredAnnotation) this.applicationContext.getBean(AutowiredAnnotation.class.getName());
        Assert.assertEquals(bean.toString(), "AutowiredAnnotation");
        Assert.assertEquals(bean.dependentBean.toString(), "DependentBean");
    }

    @Test
    public void circularDependency() {
        this.applicationContext.refresh();
        CircularDependencyA beanA = (CircularDependencyA) this.applicationContext.getBean(CircularDependencyA.class.getName());
        CircularDependencyB beanB = (CircularDependencyB) this.applicationContext.getBean(CircularDependencyB.class.getName());
        Assert.assertEquals(beanA.toString(), "CircularDependencyA");
        Assert.assertEquals(beanA.toString(), "CircularDependencyA");
        Assert.assertEquals(beanA.circularDependencyB.toString(), "CircularDependencyB");
        Assert.assertEquals(beanB.circularDependencyA.toString(), "CircularDependencyA");
    }
}
