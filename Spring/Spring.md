
# Spring

## IOC简介

本质: 由Spring IOC 容器管理 bean的创建和装配

包: `org.springframework.beans` 和 `org.springframework.context`


Spring 配置:
```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xsi:schemaLocation="http://www.springframework.org/schema/beans
        http://www.springframework.org/schema/beans/spring-beans.xsd">

    <!-- 可分层次配置bean -->
    <import resource="services.xml"/>
    <import resource="resources/messageSource.xml"/>
    <import resource="/resources/themeSource.xml"/>

    <bean id="accountDao"
        class="org.springframework.samples.jpetstore.dao.jpa.JpaAccountDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <bean id="itemDao" class="org.springframework.samples.jpetstore.dao.jpa.JpaItemDao">
        <!-- additional collaborators and configuration for this bean go here -->
    </bean>

    <!-- more bean definitions for data access objects go here -->

</beans>
```

> spring 2.5后支持@注解配置, 3.0后支持java配置


基础接口: `BeanFactory` 接口, `ApplicationContext` 类是 `BeanFactory` 的子接口
    `ApplicationContext` 集成了 :
    1. AOP
    2. 消息资源处理(用于国际化)
    3. Event publication
    4. 特定于应用程序层的上下文，例如用于 Web 应用程序的 `WebApplicationContext`

`ApplicationContext` 的获取:
     一般用 xml 文件配置spring 使用 `ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");`

`bean` 的获取:
    `Bean bean = context.getBean("bean", Bean.class);`



## IOC 容器的初始化

以`ClassPathxmlApplicationContext`为例

`refresh()`

​		`prepareRefresh()` 刷新环境前的准备(激活容器,处理配置文件占位符,做一些校验等)

​		==`obtainFreshBeanFactory()`:==

​				`refreshBeanFactory()`:

​						==`createBeanFactory()`: 创建一个 `DefaultListableBeanFactory`;==

​						`customizeBeanFactory()`设置是否覆盖bean by name, 设置是否允许循环引用

​						==`loadBeanDefinisions()`: 加载 `BeanDefinitions` 到 `beanDefinitionMap` -- 一个 `ConcurrentHashMap`==

​								==使用 `beanDefinitionReader` 处理`resource`, 将`resource` 转换为 `BeanDefinition`==

​		`prepareBeanFactory()`:

​				添加 `AwareProcessor` -- `Aware` 接口相关, 给 bean 注入 `Aware` 接口需要的属性

​				添加 `ListenerDetector` -- `ApplicationListener` 接口相关, 如果 bean 实现接口,则注册到 `Linstener Set`

​				添加 `LoadTimeWeaverAwareProcessor`

​				对一些特殊的 Aware 接口进行屏蔽, 这些接口由 `AwareProcessor` 处理

​				注入特殊 bean（`environment`、`systemProperties`、`systemEnvironment`）

​		`postProcessBeanFactory()`: 拓展点

​		`invokeBeanFactoryPostProcessors()`:  实例化并`invok` `BeanFactoryPostProcessors// 注 所有BeanFactoryPostProcessor 要在 单例实例化之前调用

​		`registerBeanPostProcessors()`: 注册 `BeanPostProcessor`

​		`initMessageSource()`: 初始化 MessageSource，国际化相关，非重点，直接跳过

​		`initApplicationEventMulticaster()`: 初始化 ApplicationEventMulticaster，事件广播器，跳过

​		`onRefresh()`: 拓展点 用于手动初始化 特殊的bean

​		`registerListeners()`: 注册监听器

​		==`finishBeanFactoryInitialization()`: 实例化所有非懒加载的 单例 bean==

​				==getBean() --> doGetBean() --> createBean() --> doCreateBean()==

​						==createBeanInstance() : 通过反射, 用 BeanDefinition 创建 bean 的实例 // 注 所有 bean 单例 存在 Singletons -- 一个 `ConcurrentHashMap`==

​						==populateBean(): 给 bean 实例注入属性值==



## Bean 的声明周期

Bean , 从创建到最后销毁 都由 IOC 容器管理

大体经过:

------------ 容器准备阶段 -------------------

- 创建 BeanFactory -- DefaultListableBeanFactory
- 加载 BeanDefinistion

-------------- bean 生命周期 --------------

- 实例化 doCreate() -> instantate() 用一个工厂, 用反射方法, 由BeanDefinition 获得信息
- 设置属性 doCreate() -> populate
- Aware 接口 -> Aware接口处理器
- 前置处理 -> PostProcessor 
- 检查是否实现 init-method 方法
- 后置处理 -> PostProcessor AOP 也在此实现, AbstractAutoProxyCreator
  - 至此Bean完全构造完成
- 使用 bean
- 判断是否实现 DisposableBean 接口
- destory() 方法



## Bean的循环依赖问题 ----- 三级缓存+提前暴露对象

### 原理

利用缓存, 把实例化和初始化分开进行, 使对象提前暴露, 令bean 可以提前拿到完成实例化,但还没有完成初始化的依赖bean,从而解决循环依赖的问题.

### 三级缓存

```java
// 三级缓存
Map<String, Object> singletonObjects = new ConcurrentHashMap<>(256); //一级缓存 存放完整 bean
Map<String, Object> earlySingletonObjects = new HashMap<>(16); // 二级缓存 存放半成品 bean
Map<String, ObjectFactory<?>> singletonFactories = new HashMap<>(16); // 三级缓存 存放bean工厂对象

// 两个状态 set
Set<String> singletonsCurrentlyInCreation // 创建中
Set<String> alreadyCreated // 创建完成
```



当对象完成了实例化和初始化之后,再把完整的实例放入容器中. 使用不同的Map来存储不同状态的bean, 这里就引出了 一级二级 缓存. 二级缓存中不会存在一级缓存中的同名对象. 三级对二级也一样.

所有对象先把工厂方法放入三级缓存, 有人引用则从三级缓存移入二级缓存,完成完整的创建,经过检查后放入一级缓存

最终 一级缓存 ==singletonObjects== 存储的是完整的实例, 二级缓存==earlySingletonObjects==中存储的是半成品的bean, 三级缓存==singletonFactories==的类型是 ObjectFactory, 是一个函数式接口, 为了保证整个IOC容器的运行过程中, 同名的bean对象只有一个(对象,aop代理对象,ObjectFactory中的函数式接口,方便aop代理)



三级缓存主要解决 代理对象的问题 addSingletonFactory

二级缓存主要解决循环依赖的问题	getSingelton

一级缓存作为最终使用的缓存 addSingelton

### 适用的情景

- 构造器注入不能自动解决循环依赖: 报错
- prototype 的属性注入不能解决循环依赖: 使用时报错 (因为非单例bean默认不会初始化,到使用时才会初始化)

- 普通单例bean 属性注入: 可以解决循环依赖问题: 根据原理, 利用 bean 的 中间态 + 三级缓存

## BeanFactory 和 FactoryBean

都是用来创建 bean 的

BeanFactory 太复杂, 需要遵守 spring 约束

FactoryBean适合自己自定义创建 bean 的时候使用: 实现以下接口定义

	- isSingleton()
	- getObjectType()
	- getObject()

## Spring中用到的设计模式

- 单例
- 原型
- 工厂模式: beanFactory
- 模板方法: onRefresh, postProcessBeanFactory .....
- 策略模式: 各种不同配置方式的 Context
- 代理模式: AOP
- 观察者: listener, event, multicast
- 适配器: adapter
- 装饰器: BeanWrapper
- 责任链: 拦截器链
- 委托: delegate



## AOP

### 原理

使用动态代理, 底层由 jdk + CGLIB 实现

在 bean 创建时, 由 BeanPostProcessor  类进行增强处理, 对原生对象进行包装, 主要是根据不同的 advice 生成一个方法调用链 然后有一个 createProxy() 方法, 使用 jdk / CGLIB 创建代理对象,在IOC容器中,最后能够拿到的对象是经过代理的代理对象, 

执行时会先过滤方法是否需要代理, 然后按照 调用链去执行各个 advice 中指定的方法.

### Avice 执行顺序

```
around before advice
before advice
target method 执行
around after advice
after advice
afterReturning
===============分割线==============
around before advice
before advice
target method 执行
around after advice
after advice
afterThrowing:异常发生
java.lang.RuntimeException: 异常发生
```



使用: 

1. 定义切面
2. 定义切入点
3. 定义 advice
4. 定义增强方法

### 事务回滚如何实现

事务是 AOP 的一项应用, 通过TransactionInterceptor 实现，调用invoke()来实现具体的逻辑

​		分：1、先做准备工作，解析 事务相关的属性，根据具体的属性,传播类型,来判断是否开始新事务

​				2、当需要开启的时候，获取数据库连接( ConnectionHolder )，关闭自动提交功能，开起事务

​				3、执行具体的sql逻辑操作

​				4、在操作过程中，如果执行失败了，那么会通过 ==completeTransactionAfterThrowing== 看来完成事务的回滚操作，回滚的具体逻辑是通过==doRollBack==方法来实现的，实现的时候也是要先==获取连接对象，通过连接对象来回滚==

​				5、如果执行过程中，没有任何意外情况的发生，那么通过==commitTransactionAfterReturning==来完成事务的提交操作，提交的具体逻辑是通过==doCommit==方法来实现的，实现的时候也是要==获取连接，通过连接对象来提交==

​				6、==当事务执行完毕之后需要清除相关的事务信息cleanupTransactionInfo==

如果想要聊的更加细致的话，需要知道TransactionInfo,TransactionStatus,



## 事务的传播特性

7 种, 常用的 3 种, 定义不同的事务方法 嵌套时 如何处理, 提交/回滚 + 内层方法如何影响外层方法

- Required				加入当前事务, 等于共享同一个事务了
- Requires New       新建事务，如果当前存在事务，把当前事务挂起。内层异常会触发外层, 外层异常对内层无影响
- Nested  		         外层失败回滚, 内层失败不会引起外层自动回滚(原因是内层回滚之后把回滚标志清理了), 只会给外层一个 Exception, 看外层怎么处理.

------------------------------

- Support
- Not_Support
- Never
- Mandatory



## 事务隔离级别

Default:  使用数据库本身使用的隔离级别 ORACLE（读已提交） MySQL（可重复读）
READ_UNCOMITTED	读未提交（脏读）最低的隔离级别，一切皆有可能。
==READ_COMMITED==	读已提交，ORACLE默认隔离级别，有幻读以及不可重复读风险。 常用
==REPEATABLE_READ==	可重复读，解决不可重复读的隔离级别，但还是有幻读风险。常用
SERLALIZABLE	串行化，最高的事务隔离级别，不管多少事务，挨个运行完一个事务的所有子事务之后才可以执行另外一个事务里面的所有子事务，这样就解决了脏读、不可重复读和幻读的问题了


![image-20211212163346455](D:\MyGitProjectWorkSpace\MyNotebook\Spring\pic\事务隔离级别.png)



## SpringBoot 对比 Spring 

- 简化了配置, 提供了一些常用依赖的自动配置
- 内嵌了 tomcat, jetty, netty 等容器, 不需要额外部署,就能跑起来
- 提供了 starter 形式的依赖包方便引用
