## Spring文档学习

### IoC
所涉及的包： `org.springframework.beans `and `org.springframework.context`
由[**`BeanFactory`**](https://docs.spring.io/spring-framework/docs/5.3.10/javadoc-api/org/springframework/beans/factory/BeanFactory.html) 提供管理机制。 
[`ApplicationContext`](https://docs.spring.io/spring-framework/docs/5.3.10/javadoc-api/org/springframework/context/ApplicationContext.html) 接口作为其子接口，在其之上附加了几项功能：
1. 易于与 Spring’s AOP 结合
2. Message resource handling (消息资源处理(用于国际化))
3. Event publication
4. Application-layer specific contexts (应用程序级上下文管理)

Bean简单来说就是应用里各种可被实例化、可装配的 Object(对象),这些对象由 container(IoC容器) 根据 config matedata(配置元信息, 即配置文件,注解等)来管理

ApplicationContext 接口的实现类主要有 2个 [`ClassPathXmlApplicationContext`](https://docs.spring.io/spring-framework/docs/5.3.10/javadoc-api/org/springframework/context/support/ClassPathXmlApplicationContext.html) 和 [`FileSystemXmlApplicationContext`](https://docs.spring.io/spring-framework/docs/5.3.10/javadoc-api/org/springframework/context/support/FileSystemXmlApplicationContext.html)