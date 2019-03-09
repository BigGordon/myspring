package com.zyc.ioc;

import com.sun.istack.internal.Nullable;
import com.zyc.ioc.annotation.Autowired;
import com.zyc.ioc.annotation.Bean;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * IoC容器实现类
 * @author Gordon
 * @since 2018/12/25
 */
public class ApplicationContext implements BeanFactory {

    /** 是否开启Aop功能 */
    private boolean usingAop = false;

    /** 用来扫描注解的包 */
    private String packageToScan = this.getClass().getPackage().getName();

    /** 用于开启Aop功能的代理工厂Bean类 */
    private String aopProxyFactoryBeanName = "com.zyc.aop.ProxyFactoryBean";

    /** 已经创建了的单例模式的Bean */
    private Map<String, Object> singletonCache = new ConcurrentHashMap<>();

    /** 预先储存好的Bean定义信息 */
    private Map<String, BeanDefinition> beanDefinitions = new ConcurrentHashMap<>();

    /** 初步构造出来的Bean实例，尚未进行属性注入，用于支持循环依赖 */
    private Map<String, Object> earlySingletonObjects = new ConcurrentHashMap<>();

    @Override
    public Object getBean(String name) throws BeansException {
        return doGetBean(name, null, null);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
        return doGetBean(name, requiredType, null);
    }

    @Override
    public Object getBean(String name, Object... args) throws BeansException {
        return doGetBean(name, null, args);
    }

    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return doGetBean(requiredType.getName(), requiredType, null);
    }

    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return doGetBean(requiredType.getName(), requiredType, args);
    }

    @SuppressWarnings("unchecked")
    protected <T> T doGetBean(final String name, @Nullable final Class<T> requiredType, @Nullable final Object[] args) throws BeansException {

        //要返回的Bean
        Object bean = null;

        //尝试重缓存中获取Bean
        if (singletonCache.containsKey(name)) {
            bean = singletonCache.get(name);
        }
        //循环依赖时，获取尚未初始化完毕的Bean
        else if (earlySingletonObjects.containsKey(name)) {
            bean = earlySingletonObjects.get(name);
        }
        else {
            //根据name取得预先定义好的Bean信息
            BeanDefinition beanDefinition = beanDefinitions.get(name);
            if (beanDefinition == null) throw new BeansException("No such bean: " + name);

            //根据Bean的定义信息创建Bean
            //AOP功能即在此拦截并生成代理类
            if(this.usingAop && !name.equals(aopProxyFactoryBeanName)) {
                FactoryBean proxyFactoryBean = (FactoryBean) getBean(aopProxyFactoryBeanName);
                if(proxyFactoryBean != null) {
                    bean = proxyFactoryBean.createBean(beanDefinition);
                } else {
                    throw new BeansException("FactoryBean not found. ");
                }
            }
            else {
                //利用构造器反射生成Bean实例
                if (beanDefinition.getConstructor() != null) {
                    try {
                        bean = beanDefinition.getConstructor().newInstance(args);
                    } catch (Exception e) {
                        throw new BeansException(e);
                    }
                } else {
                    throw new BeansException("Bean constructor not found. ");
                }
            }

            //将初步实例化的Bean放入初步单例缓存，用于循环依赖
            if (beanDefinition.isSingleton()) {
                earlySingletonObjects.put(name, bean);
            }

            //装配Bean中的属性
            Map<Field, String> properties = beanDefinition.getProperties();
            if (properties != null && properties.size() > 0) {
                for (Map.Entry<Field, String> entry : properties.entrySet()) {
                    Field field = entry.getKey();
                    String value = entry.getValue();
                    Object propertyBean = this.doGetBean(value, null, null);
                    //利用反射设置域值
                    try {
                        field.set(bean, propertyBean);
                    } catch (IllegalAccessException e) {
                        throw new BeansException(e);
                    }
                }
            }

            //将初始化完毕的实例加入单例缓存
            if (beanDefinition.isSingleton()) {
                singletonCache.put(name, bean);
                earlySingletonObjects.remove(name);
            }
        }

        //检查返回的Bean是否符合指定类型
        if (requiredType != null && !requiredType.isInstance(bean)) throw new BeansException(
                "Bean does not match requiredType: " + requiredType.getName() + " . "
        );

        return (T) bean;
    }

    //刷新IOC容器，扫描@Bean
    @SuppressWarnings("unchecked")
    public void refresh() {
        //扫描启动类所在包里所有的类
        Set<Class<?>> classes = Utils.getClasses(packageToScan);
        if (classes == null) {
            return;
        }
        //将含有@Bean注解的类写入BeanDefinition
        for (Class clazz : classes) {
            if(clazz != null && clazz.getAnnotation(Bean.class) != null) {
                if (clazz.getName().equals(aopProxyFactoryBeanName)) {
                    this.usingAop = true;
                }
                BeanDefinition beanDefinition = new BeanDefinition();
                beanDefinition.setSingleton(true);
                beanDefinition.setClazz(clazz);
                beanDefinition.setApplicationContext(this);
                try {
                    beanDefinition.setConstructor(clazz.getConstructor());
                } catch (Exception e) {
                    throw new BeansException(e);
                }
                //检查带@Autowired、需要自动注入的域
                Map<Field, String> properties = new HashMap<>();
                Field[] fields = clazz.getDeclaredFields();
                if (fields != null) {
                    for (Field field : fields) {
                        if (field != null && field.getAnnotation(Autowired.class) != null) {
                            properties.put(field, field.getType().getName());
                        }
                    }
                }
                beanDefinition.setProperties(properties);
                this.beanDefinitions.put(clazz.getName(), beanDefinition);
            }
        }
    }

    @Override
    public boolean containsBean(String name) {
        return beanDefinitions.containsKey(name) || singletonCache.containsKey(name);
    }

    @Override
    public boolean isSingleton(String name) throws BeansException {
        if (!beanDefinitions.containsKey(name)) throw new BeansException("No such bean: " + name + " . ");
        return singletonCache.containsKey(name);
    }

    @Override
    public Class<?> getType(String name) throws BeansException {
        if (!beanDefinitions.containsKey(name)) throw new BeansException("No such bean: " + name + " . ");
        return beanDefinitions.get(name).getClazz();
    }

    public Map<String, BeanDefinition> getBeanDefinitions() {
        return beanDefinitions;
    }

    public void setPackageToScan(String packageToScan) {
        this.packageToScan = packageToScan;
    }

    public String getPackageToScan() {
        return packageToScan;
    }
}
