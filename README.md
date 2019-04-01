# MySpring #
MySpring是一个**IOC/AOP工具**，是传统Spring项目核心模块的一个轻量级实现，是快速构建Java应用程序优良选择。  
功能齐全，代码简洁，有详细的原理**说明文档**和**中文注释**，也是入门Spring核心功能源码实现的优良选择。
## 主要特点 ##
- 基于注解的依赖注入和BeanDefinition配置
- 抽象工厂模式实现IOC模块与AOP模块解耦
- 临时缓存解决循环依赖
- 基于CGLIB动态生成代理类
## 如何使用MySpring ##
1. 下载MySpring项目源码，生成jar包放入工程的类目录下  
使用**IOC容器**：打包`myspring-ioc`模块  
使用**IOC容器+AOP功能**：打包`myspring-ioc`、`myspring-aop`模块  
推荐使用IDEA配合gradle进行打包
2. 添加IOC、AOP模块的相关注解后，在工程启动类中初始化MySpring    
```java
    public ApplicationContext applicationContext;//IOC容器的引用
    public static void main(String[] args) {
	//生成IOC容器，推荐以单例模式使用
        applicationContext = new ApplicationContext();
	//传入Bean类所在的包，用以扫描相关注解
        applicationContext.setPackageToScan(this.getClass().getPackage().getName());
	//初始化IOC容器之后即可使用
        applicationContext.refresh();
    }
```
3. 使用时调用getBean()方法,传入Bean的全限定类名即可得到相应实例  
```java
	Bean bean = applicationContext.getBean(Bean.class.getName());
```
  
## 注解使用说明 ##
### IOC  
- @Bean  
用于**类**上，表示该类为一个Bean，在IOC容器的管理之下，默认为**单例模式**  
该类需要含有**无参构造函数**
- @Autowired  
用于类中**域**上，**该类必须用@Bean标记**，IOC容器会对该域进行自动注入  
```java
    //@Bean和@Autowired注解演示
    @Bean
    public static class Bean {
	//容器会自动取到DependentBean实例并注入该域
        @Autowired
        DependentBean dependentBean;
    }

    @Bean
    public static class DependentBean {
    }
```
### AOP  
- @Aspect  
用于**类**上，该类定义一个切面，表示一次AOP行为  
需要在该类中的方法上标注@Pointcut和@Advice  
**该类需要同时标注@Bean**
- @Pointcut  
用于**方法**，该类需要标记@Aspect，需要指明**targetClass**、**targetMethod**两个属性。该方法体内部的内容不起作用，将被忽略  
targetClass需要指明被增强类的全限定名，一次只能一个类  
targetMethod需要指明被增强方法，可以指定多个，用逗号隔开  
可选属性allMethods默认为false  
- @Advice  
用于**方法**，该类需要标记@Aspect，表示具体的增强方法，这些被标注的方法将在被增强方法调用前实现  
```java
//定义一个切面，并将其交予IOC容器管理
@Aspect
@Bean
public class GeneralAdvisor {

    //注明目标类与需要被增强的方法
    @Pointcut(targetClass = "com.zyc.aop.classesForTest.TargetClass", targetMethod = "targetMethodA")
    public void pointcut() {}

    //具体的增强方法
    @Advice
    public void advice() {
        System.out.println("Advice");
    }
}
```
## 核心功能详解 ##
建议配合源码阅读
### IOC具体是怎么实现的  
- BeanDefinition的加载  
Ioc容器在工作时需要BeanDefinition来实例化对应的Bean，因此需要在容器启动时将各个Bean对应的BeanDefinition加载到容器里。这里定义了一个@Bean注解，容器启动时会扫描工作包，当检测到类上的@Bean注解时，就会以Bean的类名为键，新建的BeanDefinition为值存到容器中的一个ConcurrentHashMap里。BeanDefinition储存着Bean的类名、生成工厂、该Bean所依赖的Bean等相关信息。
- getBean()的实现  
用户通过Bean的类名，通过getBean()方法向ioc容器请求Bean实例。Bean默认为单例模式。容器会先查看单例缓存，若已经生成过了则会直接取出并返回。单例缓存也是个ConcurrentHashMap，先前生成过的Bean实例会存在这里。若缓存里没有，则会开始生成。Bean的生成分为两步，一是该Bean类的实例化，这里调用BeanDefinition的Bean工厂来实现，若启动AOP，这里会返回相应代理类，否则默认反射调用无参构造函数进行实例化；二是加载该Bean所依赖的Bean，这里需要提前在该Bean类中在对应字段添加@Autowired注解，ioc容器会根据Bean的类名递归调用getBean()对实例进行获取，并通过反射注入到相应的域里。两个步骤完毕后即返回完整的Bean实例。
### AOP具体是怎么实现的
IOC容器在会利用BeanDefinition里的Bean工厂来生成Bean就，若启用AOP，则容器会调用代理Bean工厂来生成代理Bean实例，AOP功能就是通过在这里进行偷梁换柱而实现的。这个代理Bean实例对用户来说和原来的Bean实例完全一致，只是在代理Bean中，加入了新的切面增强方法。

AOP模块启动时把代理Bean工厂作为Bean加入IOC容器。代理Bean工厂类第一次启动时会扫描工作包，寻找@Aspect注解标注的切面类。一个切面类代表一个AOP行为，需要用@Pointcut注解记录一个切口，包括目标类和目标方法，同时用@Advice注解标注增强方法。代理Bean工厂针对每一个切面生成一个Advisor类，记录着具体的切口和增强方法，同时将所有Advisor缓存，等到生成代理Bean时使用。

代理Bean的生成利用cglib实现。Cglib底层采用asm字节码生成框架生成代理类的字节码。使用cglib时需要自定义拦截器MethodInterceptor，重写拦截方法intercept来添加增强方法。通过遍历缓存的Advisor得知当前的类为目标类时，会将相应的Advice加进来，通过反射的方法在目标方法执行前调用增强方法，即完成了AOP的功能。
### 利用抽象工厂方法实现IOC、AOP模块的解耦是怎么做到的  
IOC、AOP的解耦指的是当只需要IOC功能时，只引入IOC的jar包也是可以工作的，IOC的实现并不依赖AOP模块。当然，反之AOP的实现需要依赖IOC模块。IOC容器利用Bean工厂生成Bean实例，这里的Bean工厂是一个接口，根据不同的需求可以选用不同的Bean工厂。当开启AOP功能时，容器会调用实现Bean工厂接口的代理Bean工厂来生成Bean；当关闭AOP功能时，容器则使用默认的Bean工厂生成默认的Bean实例。
### 循环依赖指的是什么，是怎么解决的  
- 循环依赖：例如BeanA内部有个BeanB的域，需要IOC容器来注入一个BeanB实例，而BeanB内部也有个BeanA的域，需要注入一个BeanA实例。当触发BeanA的getBean时，需要注入BeanB，而BeanB还未加载，因此触发BeanB的getBean，而BeanB需要注入一个BeanA，而BeanA也还没加载，需要重新getBean，结果导致死循环。
- 解决方法：除了Bean完成两步初始化完整生成后会放入单例缓存之外，在Bean完成第一步初始化，尚未加载依赖的Bean时，也会放入一个早期缓存。当循环依赖发生时，BeanA完成第一步初始化，把自己放入早期缓存，进而通过getBean去加载自己的依赖BeanB。BeanB第一步初始化后，也需要去加载自己的依赖BeanA，但并不会去getBean，而是去早期缓存查看是否有BeanA，在拿到BeanA实例的引用之后，BeanB的两步初始化也就完成了，从而BeanA也完成的两步初始化，避免了死循环
### CGLIB是怎么生成动态代理类的，和jdk的代理有什么区别  

- CGLIB是生成的代理类是被代理对象的子类，因此无法代理final方法或者final类；jdk生成的代理对象则是和被代理对象实现同一个接口，因此需要提供一个接口类。
- CGLIB和jdk都是在运行期生成字节码，CGLIB使用ASM框架写Class字节码，而jdk是直接写字节码。
