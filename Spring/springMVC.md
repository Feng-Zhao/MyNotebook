# Sping MVC

## DispatcherServlet
用于对用户请求做总体调度

流程:
请求 --> 

1. DispatcherServlet --> 
2. DispatcherServlet.doService() -->
3. HandlerMapping -->  (根据 URL 查找 handler(Controller))
4. HandlerExecution --> DispatcherServlet --> 
5. HandlerAdapter (找具体的Controller,让其执行)
6. Controller --> 
7. HandlerAdapter  --> DispatcherServlet --> 
8. ViewResolver (获取 ModelAndView 中的数据, 解析 ModelAndView 中视图的名字, 找到对应的视图, 将数据渲染到视图中)



大致流程:
DispatcherServlet 接受请求,调用 HandlerMapping 找到需要的 handler, 
将 handler 交给 HandlerAdapter 去实际调用 Controller 里对应的方法,
其结果由 HandlerAdapter 交给 DispatcherServlet,
DispatcherServlet 找 视图解析器 ViewResolver 去解析和渲染视图,
视图解析器将渲染后的视图交给 DispatcherServlet ,
DispatcherServlet 将结果返还给用户.



## 配置

web.xml

```xml
    <servlet>
        <servlet-name>springmvc</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <!-- mvc 配置文件 -->
            <param-value>classpath:springmvc-servlet.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>springmvc</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>
```

springmvc-servlect.xml

```xml
<?xml version="1.0" encoding="utf-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xmlns:mvc="http://www.springframework.org/schema/mvc"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
       http://www.springframework.org/schema/beans/spring-beans.xsd http://www.springframework.org/schema/context https://www.springframework.org/schema/context/spring-context.xsd http://www.springframework.org/schema/mvc https://www.springframework.org/schema/mvc/spring-mvc.xsd">

    <!-- 处理器映射器 -->
<!--    <bean class="org.springframework.web.servlet.handler.BeanNameUrlHandlerMapping"/>-->
    <!-- 处理器适配器 -->
<!--    <bean class="org.springframework.web.servlet.mvc.SimpleControllerHandlerAdapter"/>-->

    <!-- 视图解析器 -->
    <bean class="org.springframework.web.servlet.view.InternalResourceViewResolver"
    id="internalResourceViewResolver">
        <property name="prefix" value="/WEB-INF/jsp/"/>
        <property name="suffix" value=".jsp"/>

    </bean>

    <!-- 自动扫描 controller 包-->
    <context:component-scan base-package="xyz.francis.controller"/>

    <!-- 过滤静态资源 -->
    <mvc:default-servlet-handler/>

    <!-- 启用 MVC 注解, 会自动注册 DefaultAnnotationHandlerMapping 和 AnnotationMethodHandlerAdapter -->
    <!-- 即自动注册 -->
    <!-- 1.处理器适配器 -->
    <!-- 2.处理器映射器 -->
    <mvc:annotation-driven/>
    
</beans>
```

## Controller 编写

**注解写法:**

```java
package xyz.francis.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
//@RequestMapping("/controller")
public class HelloController {

    @RequestMapping("/hello")
    public String Hello(Model model){
        model.addAttribute("msg","Hello Spring MVC Annotation");
        return "hello"; // 视图解析器会处理返回值, 找到配置好的目录下的 hello.jsp
    }
}

```



**实现 Controller 接口写法**

```java
/**
 * 最原始的 Controller ,实现 Controller 接口
 * 实现 handleRequest 方法, 方法内就是处理请求的业务逻辑
 * 返回 ModelAndView
 * 
 * 这种形式需要在 mvc 配置文件中 将类注册为 bean
 * bean 的 name 或 id 为对应的 url 地址
 * <bean id="/hello" class="xyz.francis.controller.HelloController"/>
 *
 * 缺点,一个Controller只能写一个方法,对应一个地址
 */
public class HelloController implements Controller {
    @Override
    public ModelAndView handleRequest(HttpServletRequest request, HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView();
        // 设置数据
        mv.addObject("msg","helloSpringMVC");
        // 设置视图的名字
        mv.setViewName("hello");
        return mv;
    }
}
```



## 转发 重定向

```java
// 视图解析器默认转发
return ("/test");
// 显式定义转发,不走视图解析器需要完整路径
return ("forward:/WEB-INF/jsp/test.jsp");
// 重定向 访问不到 /WEB-INF 目录, 该目录受保护
return ("redirect:/test.jsp");
```



##  参数传递

```Java
@Controller
@RequestMapping("/obj")
public class ParamController {

    /**
     * /obj/test1?name=xxx 传递参数
     */
    @RequestMapping("/test1")
    public String test1(String name, Model model){
        model.addAttribute("msg","name = " + name);
        return "hello";
    }

    /**
     * 通过 @RequestParam("value") 定义别名,并且约束必须传递 该参数
     * /obj/test2?value=xxx 传递参数
     */
    @RequestMapping("/test2")
    public String test2(@RequestParam("value") String name, Model model){
        model.addAttribute("msg","name = " + name);
        return "hello";
    }

    /**
     * 传递 obj 参数
     * 需要参数名和对象的属性名 一致
     * /obj/test3?name=xxx&age=18 传递参数
     */
    @RequestMapping("/test3")
    public String test3(User user, Model model){
        model.addAttribute("msg","name = " + user.getName() + "age=" + user.getAge());
        return "hello";
    }
}
```



## 使用@Controller时的 RestFul 风格

**使用 @PathVariable 映射 URL 绑定的占位符!!** 

```java
/**
 * RestFul 风格,用路径来传递参数, 使用 @PathVariable 设置 参数从 url 中获取
 * @PathVariable 映射 URL 绑定的占位符!!
 * 有缓存,比较高效
 */

/**
 * @GetMapping 直接配置 method=RequestMethod.GET
 */
@GetMapping("/getConcat/{a}/{b}")
public String test3(@PathVariable String a, @PathVariable String b, Model model){
        String result = a + b;
        model.addAttribute("msg","GetMapping结果为:"+result);
        return "hello";
}

@PostMapping("/getConcat/{a}/{b}")
public String test4(@PathVariable String a, @PathVariable String b, Model model){
        String result = a + b;
        model.addAttribute("msg","PostMapping结果为:"+result);
        return "hello";
}
```



**注: 关于 @RestController**

> ```
> /**
>  * 不使用 @RestController 时,完成 RestFul 风格
>  * 注: @RestController = @Controller + @ResponseBody
>  * 注: @ResponseBody 注解的作用是将controller的方法返回的对象通过适当的转换器转换为指定的格式之后，写入到response对象的body区，通常用来返回JSON数据或者是XML数据。
>  * 注：在使用此注解之后不会再走视图处理器，而是直接将数据写入到输入流中，他的效果等同于通过response对象输出指定格式的数据。
>  */
> ```



## 数据回显

```java
	/**
	 * 通过 Model
	 * model.addAttribute("msg","name = " + name);
	 * Model 继承关系 
	 * LinkedHashMap <-extends- ModelMap -implements-> Model
	 */

	@RequestMapping("/test1")
    public String test1(String name, Model model){
        model.addAttribute("msg","name = " + name);
        return "hello";
    }

	@RequestMapping("/test4")
    public String test1(String name, ModelMap modelmap){
        modelmap.addAttribute("msg","msg from ModelMap");
        return "hello";
    }

    /**
	 * 通过 ModelAndView
	 * ModelAndView 包含 ModelMap 
	 * 此外还 包含 Object view, HttpStatus status
	 *
	 * public class ModelAndView {
     *  @Nullable
     *  private Object view;
     *  @Nullable
     *  private ModelMap model;
     *  @Nullable
     *  private HttpStatus status;
     *  rivate boolean cleared = false;
     *  }
	 *
	 */
    public ModelAndView handleRequest(HttpServletRequest request, 		HttpServletResponse response) throws Exception {
        ModelAndView mv = new ModelAndView();
        // 设置数据
        mv.addObject("msg","helloSpringMVC");
        // 设置视图的名字
        mv.setViewName("hello");
        return mv;
    }

	
```



## 乱码问题

### 使用 filter

```java
public class EncodingFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        servletRequest.setCharacterEncoding("UTF-8");
        servletResponse.setCharacterEncoding("UTF-8");
        filterChain.doFilter(servletRequest,servletResponse);
    }

    @Override
    public void destroy() {

    }
}
```



在 web.xml 中配置 filter

```xml
<filter>
        <filter-name>encoding</filter-name>
        <filter-class>xyz.francis.filter.EncodingFilter</filter-class>
    </filter>

	<!-- 这里配置 /* 表示过滤所有请求 -->
	<!-- 如果改为 / 则无法处理对jsp的请求 -->
    <filter-mapping>
        <filter-name>encoding</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```



### 使用Spring MVC 提供的编码过滤器

```xml
    <!-- Spring MVC 提供的编码过滤器 -->
    <filter>
        <filter-name>encoding</filter-name>
        <filter-class>org.springframework.web.filter.CharacterEncodingFilter</filter-class>
        <init-param>
            <param-name>encoding</param-name>
            <param-value>utf-8</param-value>
        </init-param>
    </filter>
    <filter-mapping>
        <filter-name>encoding</filter-name>
        <url-pattern>/*</url-pattern>
    </filter-mapping>
```

