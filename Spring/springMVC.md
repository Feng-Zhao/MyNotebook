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
// 显式定义转发,此方式不走视图解析器,需要写完整路径
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
     * get 方式, 用 url 参数传递数据
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

==使用 @PathVariable 映射 URL 绑定的占位符!!*==

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

	/**
	 * 通过 ModelMap
	 */
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

### 使用 filter 设置 request response 使用 utf8 编码

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



## 拦截器

### 实现 HandlerInterceptor

```java
public class LoginInterceptor implements HandlerInterceptor {
    private final Logger logger = LoggerFactory.getLogger(LoginInterceptor.class);
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        logger.debug(" ==== 进入LoginInterceptor ====");
        HttpSession session = request.getSession();
        if(session == null){
            logger.debug("未登录,跳转到登陆页面");
            request.getRequestDispatcher("/interceptor/goLogin").forward(request,response);
            return false;
        }
        if(request.getRequestURL().toString().contains("/login")){
            logger.debug("从登录页面来,放行");
            return true;
        }
        if(session.getAttribute("userInfo") != null){
            logger.debug("已登录,放行");
            return true;
        }
        else{
            logger.debug("session中无登陆数据,跳转到登录页面");
            request.getRequestDispatcher("/interceptor/goLogin").forward(request,response);
            return false;
        }

    }
}
```

### 拦截器路径配置

```xml
<mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/book/**"/>
            <bean class="xyz.francis.interceptor.TestInterceptor"/>
        </mvc:interceptor>
        <mvc:interceptor>
            <mvc:mapping path="/interceptor/main"/>
            <bean class="xyz.francis.interceptor.LoginInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>
```



## 文件上传

导包

```xml
<dependency>
    <groupId>commons-fileupload</groupId>
    <artifactId>commons-fileupload</artifactId>
    <version>1.4</version>
</dependency>
```

配置 

```xml
<bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
    <property name="defaultEncoding" value="utf-8"/>
    <!-- 文件上传大小 10MB-->
    <property name="maxUploadSize" value="10485760"/>
    <!-- 文件一次占用内存大小 -->
    <property name="maxInMemorySize" value="40960"/>
</bean>
```

前端

设置 `enctype="multipart/form-data"`

```jsp
<form action="/file/upload" method="post" enctype="multipart/form-data">
    <input type="file" id="file" name="file">
    <input type="submit" value="上传文件">
</form>
${msg}
```

后端

```java
@Controller
@RequestMapping("/file")
public class FileController {
    private final Logger logger = LoggerFactory.getLogger(FileController.class);
    @PostMapping("/upload")
    public String test09(@RequestParam("file") MultipartFile multipartFile, HttpServletRequest request, Model model) throws IOException {
        // 获取文件上传到具体文件夹的绝对路径
        String realpath = request.getSession().getServletContext().getRealPath("upload");
        // 获取上传的文件名
        String fileName = multipartFile.getOriginalFilename();
        //为了确保上传文件的重名问题，对文件名进行处理
        fileName = UUID.randomUUID().toString() +"_"+ fileName;
        // 根据路径构建文件对象
        // 在构建过程中一定要注意路径问题
        File uploadFile = new File(realpath, fileName);
        // 判断指定文件夹uploadfiles是否存在，不存在就创建
        if (!uploadFile.exists()) {
            uploadFile.mkdirs();
        }
        logger.debug(realpath);
        // 上传文件
        multipartFile.transferTo(uploadFile);
        model.addAttribute("msg","上传成功:文件存储于"+realpath);
        return "fileupload";
    }

    @RequestMapping("/download")
    public String download(){
        return "/";
    }
}
```



## 文件下载

两种方式实现文件下载

下载地址

```
localhost:8080/file/download/LICENSE
localhost:8080/file/download2?fileName=LICENSE
```

```java
/**
 * spring 封装 ResponseEntity 返回文件
 */
@GetMapping("download/{filename}")
@ResponseBody
public ResponseEntity<byte[]> download(@PathVariable String filename) throws IOException {
    //下载文件的路径(这里绝对路径)
    String filepath= "D:\\apache-maven-3.6.3\\"+filename;
    File file =new File(filepath);
    //创建字节输入流，这里不实用Buffer类
    InputStream in = new FileInputStream(file);
    //available:获取输入流所读取的文件的最大字节数
    byte[] body = new byte[in.available()];
    //把字节读取到数组中
    in.read(body);
    //设置请求头
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("Content-Disposition", "attchement;filename=" + file.getName());
    //设置响应状态
    HttpStatus statusCode = HttpStatus.OK;
    in.close();
    ResponseEntity<byte[]> entity = new ResponseEntity<byte[]>(body, headers, statusCode);
    return entity;//返回
}


@RequestMapping(value = "/download2")
@ResponseBody
public void downloadFile(String fileName, HttpServletRequest request, HttpServletResponse response){
    if(fileName != null){
        String realPath = "D:\\apache-maven-3.6.3\\";
        File file = new File(realPath, fileName);
        OutputStream out = null;
        if(file.exists()){
            //设置下载完毕不打开文件
            response.setContentType("application/force-download");
            //设置文件名
            response.setHeader("Content-Disposition", "attachment;filename="+fileName);
            try {
                out = response.getOutputStream();
                //使用工具类
                out.write(FileUtils.readFileToByteArray(file));
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }finally{
                if(out != null){
                    try {
                        out.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
```



## 附录

### 最终配置文件

#### *springmvc-servlet.xml*

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
    <mvc:annotation-driven>
        <!-- Jackson 解决中文乱码 -->
        <mvc:message-converters register-defaults="true">
            <bean class="org.springframework.http.converter.StringHttpMessageConverter">
                <constructor-arg value="UTF-8"></constructor-arg>
            </bean>
            <bean class="org.springframework.http.converter.json.MappingJackson2HttpMessageConverter">
                <property name="objectMapper">
                    <bean class="org.springframework.http.converter.json.Jackson2ObjectMapperFactoryBean">
                        <property name="failOnEmptyBeans" value="false"></property>
                    </bean>
                </property>
            </bean>
        </mvc:message-converters>
    </mvc:annotation-driven>
    <!-- 拦截器 -->
    <mvc:interceptors>
        <mvc:interceptor>
            <mvc:mapping path="/book/**"/>
            <bean class="xyz.francis.interceptor.TestInterceptor"/>
        </mvc:interceptor>
        <mvc:interceptor>
            <mvc:mapping path="/interceptor/main"/>
            <bean class="xyz.francis.interceptor.LoginInterceptor"/>
        </mvc:interceptor>
    </mvc:interceptors>
    <!-- 文件上传 -->
    <bean id="multipartResolver" class="org.springframework.web.multipart.commons.CommonsMultipartResolver">
        <property name="defaultEncoding" value="utf-8"/>
        <!-- 文件上传大小 10MB-->
        <property name="maxUploadSize" value="10485760"/>
        <!-- 文件一次占用内存大小 -->
        <property name="maxInMemorySize" value="40960"/>
    </bean>

</beans>
```



#### *web.xml*

```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_4_0.xsd"
         version="4.0">

    <servlet>
        <servlet-name>springmvc</servlet-name>
        <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
        <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:ApplicationContext.xml</param-value>
        </init-param>
        <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>
        <servlet-name>springmvc</servlet-name>
        <url-pattern>/</url-pattern>
    </servlet-mapping>

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

    <listener>
        <listener-class>xyz.francis.listener.ContextFinalizer</listener-class>
    </listener>

    <!-- session timeout -->
    <session-config>
        <session-timeout>60</session-timeout>
    </session-config>
</web-app>
```

