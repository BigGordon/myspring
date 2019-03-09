package com.zyc.aop;

import com.zyc.aop.annotation.Aspect;
import com.zyc.ioc.ApplicationContext;
import com.zyc.ioc.BeanDefinition;
import com.zyc.ioc.FactoryBean;
import com.zyc.ioc.Utils;
import com.zyc.ioc.annotation.Bean;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Gordon
 * @since 2019/2/28
 */
@Bean
public class ProxyFactoryBean implements FactoryBean {

    /** 所有登记的AOP操作 */
    private static List<Advisor> advisors = new ArrayList<>();

    /** 缓存目标类、目标类中目标方法对应的增强方法 */
    private static ConcurrentHashMap<Class, Map<Method, List<Advice>>> adviceCache = new ConcurrentHashMap<>();

    /** 是否已经扫描过包里登记的Advisor */
    private boolean advisorChainInitialzed = false;

    @Override
    public Object createBean(BeanDefinition beanDefinition) {
        return createAopProxy(beanDefinition);
    }

    private Object createAopProxy(BeanDefinition beanDefinition) {
        //扫描包里登记的Advisor
        initializeAdvisorChain(beanDefinition);
        //选择与目标类匹配的pointcut，并缓存与目标类中目标方法对应的advice
        matchPointcuts(beanDefinition);

        //以下使用cglib生成代理类
        Enhancer enhancer = new Enhancer();
        //cglib生成的代理类为原始类的子类
        enhancer.setSuperclass(beanDefinition.getClazz());
        //设置切面增强的方法
        enhancer.setCallback(new DynamicAdvisedInterceptor(beanDefinition));
        return enhancer.create();
    }

    private void initializeAdvisorChain(BeanDefinition beanDefinition) {
        if(this.advisorChainInitialzed)
            return;
        ApplicationContext ctx = beanDefinition.getApplicationContext();
        String pack = ctx.getPackageToScan();
        //扫描启动类所在包里所有的类
        Set<Class<?>> classes = Utils.getClasses(pack);
        if (classes == null) {
            return;
        }
        //将含有@Aspect注解的类写入advisors
        for (Class clazz : classes) {
            if(clazz != null && clazz.getAnnotation(Aspect.class) != null) {
                Advisor advisor = new Advisor();
                Pointcut pointcut = null;
                List<Advice> advices = new ArrayList<>();
                //查找@Pointcut和@Advice
                Method[] methods = clazz.getMethods();//该类及父类所有public方法，getDeclareFields则是该类所有方法（不含父类）
                if (methods != null) {
                    for (Method method : methods) {
                        if (method != null && method.getAnnotation(com.zyc.aop.annotation.Pointcut.class) != null) {
                            //将pointcut注解里的类设置为目标类
                            if(pointcut != null) throw new AopException("More than one Pointcut!");
                            pointcut = new Pointcut();
                            com.zyc.aop.annotation.Pointcut pcutAnno = method.getAnnotation(com.zyc.aop.annotation.Pointcut.class);
                            //设置目标类
                            String className = pcutAnno.targetClass();
                            if(className.isEmpty())
                                throw new AopException("Target class for Pointcut not set in " + clazz.getName());
                            Class targetClass = null;
                            try {
                                targetClass = Class.forName(className);
                            } catch (Exception e) {
                                throw new AopException(e);
                            }
                            if (targetClass == null)
                                throw new AopException("Class: " + className + "not found. ");
                            pointcut.setClazz(targetClass);
                            //设置目标方法
                            Method[] allMethods = targetClass.getDeclaredMethods();
                            if (pcutAnno.allMethods()) {
                                //设置目标类所有方法为目标方法
                                pointcut.setMethods(new ArrayList<>(Arrays.asList(allMethods)));
                            } else {
                                String[] targetMethodNames = pcutAnno.targetMethod().split(",");
                                if (targetMethodNames.length == 0)
                                    throw new AopException("Target methods format error");
                                Set<String> set = new HashSet<>(Arrays.asList(targetMethodNames));
                                List<Method> methodList = new ArrayList<>();
                                //遍历目标类所有方法，选取名字相符的方法
                                for (Method targetMethod : allMethods) {
                                    if (set.contains(targetMethod.getName())) {
                                        methodList.add(targetMethod);
                                    }
                                }
                                if (methodList.size() != targetMethodNames.length)
                                    throw new AopException("Target methods not found or format error");
                                pointcut.setMethods(methodList);
                            }
                        } else if (method != null && method.getAnnotation(com.zyc.aop.annotation.Advice.class) != null) {
                            //将Advice注解下的方法设置为增强方法
                            Advice advice = new Advice();
                            advice.setClazz(clazz);
                            advice.setMethod(method);
                            advices.add(advice);
                        }
                    }
                }
                if (pointcut == null) throw new AopException("No Pointcut detected");
                advisor.setPointcut(pointcut);
                advisor.setAdvices(advices);
                advisors.add(advisor);
            }
        }

        this.advisorChainInitialzed = true;
    }

    private void matchPointcuts(BeanDefinition beanDefinition) {
        if(advisors.isEmpty()) return;
        Map<Method, List<Advice>> advicesForTargetMethod = new ConcurrentHashMap<>();
        for(Advisor advisor : advisors) {
            //匹配切口类是否与目标类相同
            if(advisor.getPointcut().matches(beanDefinition.getClazz())) {
                List<Method> targetMethods = advisor.getPointcut().getMethods();
                //如果pointcut没有声明目标类则跳过
                if(targetMethods == null || targetMethods.isEmpty()) continue;
                List<Advice> advices = advisor.getAdvices();
                //将目标方法对应的增强advice缓存起来
                for(Method method : targetMethods) {
                    advicesForTargetMethod.put(method, advices);
                }
            }
        }
        adviceCache.put(beanDefinition.getClazz(), advicesForTargetMethod);
    }

    private class DynamicAdvisedInterceptor implements MethodInterceptor {

        BeanDefinition beanDefinition;

        public DynamicAdvisedInterceptor(BeanDefinition beanDefinition) {
            this.beanDefinition = beanDefinition;
        }

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
            List<Object> inters = new ArrayList<>();
            //若没有aop操作则返回执行原方法
            if(ProxyFactoryBean.advisors.isEmpty() || !adviceCache.containsKey(beanDefinition.getClazz()))
                return methodProxy.invokeSuper(obj, args);
            Map<Method, List<Advice>> map = adviceCache.get(beanDefinition.getClazz());
            List<Advice> advices = map.get(method);
            if(advices == null || advices.isEmpty())
                return methodProxy.invokeSuper(obj, args);
            for(Advice advice: advices)
                advice.getMethod().invoke(beanDefinition.getApplicationContext().getBean(advice.getClazz()));
            //执行原方法
            methodProxy.invokeSuper(obj, args);
            return obj;
        }
    }
}
