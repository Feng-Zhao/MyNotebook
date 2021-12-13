[toc]

# SpringBoot

文档地址

http://felord.cn/_doc/_springboot/2.1.5.RELEASE/_book/pages/spring-boot-features.html#boot-features

## 自动装配原理

==META-INF/spring.factories 文件, 获取 XXXAutoConfigration 类==

==@ConditionalOnXXX 条件判断是否启用 Configration 类==

```java
@SpringBootApplication ->
	@EnableAutoConfiguration ->
		@Import(AutoConfigurationImportSelector.class) ->
			getAutoConfigurationEntry()
                List<String> configurations = SpringFactoriesLoader.loadFactoryNames(getSpringFactoriesLoaderFactoryClass(),
				getBeanClassLoader()); ->
					//扫描 META-INF/spring.factories 文件, 获取配置类列表

// 该文件内记录了所有 配置类 
// 配置类中 @ConditionalOnXXX 注解 检查该配置类是否生效
```



## SpringBoot 启动流程

即

```java
@SpringBootApplication
public class SpringBootLearningApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringBootLearningApplication.class, args);
    }

}
```

到底怎么启动的

```java
run(); // -> 方法 ,静态方法
    // 实例化 SpringApplication
    /// 获取配置
    /// 推断是否为web项目
    /// bootstrapRegistryInitializers bootstrap注册服务启动器
    	// SpringApplication.run
    		/// bootstrapContext
    		/// HeadlessProperty
    		/// boostrapContext 启动监听器
    		/// 创建 ApplicationContext 实际为ConfigurableApplicationContext
    		/// prepareContext() 准备 ApplicationContext
    		/// refreshContext() 刷新环境
    		/// ApplicationContext启动监听器
    		/// 所有ApplicationRunner 和 CommandLineRunner 启动
/**
 * 最终表现为 
 * 读取配置, 
 * 缺省值自动化配置,
 * 完成 ApplicationContext -> spring 内容
 * 启动所有 ApplicationRunner 和 CommandLineRunner
```



## YAML/properties --> 配置文件

```yml
##
# yml 基础语法, k: v | obj: {} | array1: [a1,a2,a3] | array2: -a1 -a2 -a3
##

# 属性
prop: str
# 对象
obj: {prop1: a, prop2: b}
# 数组
array1: [a1,a2,a3]
array2:
    - a1
    - a2
    - a3

# 表达式
age: ${random.int}
uuid: ${random.uuid}

# 默认值
str: ${exists:default}
```



```java
// 可通过 @ConfigurationProperties(prefix = "xxx") 直接给bean赋值

@Component
@ConfigurationProperties(prefix = "xxx")
public class MyBean {
}

@Component
@PropertySource(value="xxx.properties")
public class Mybean {
    @Value("${prob}")
    String prop;
}
```

## 配置文件优先级

- file: config
- file: /
- classpath: config
- classpath: /

**注: 同目录下 properties > ymal > yml**



## 多环境配置

### properties 配置

```properties
spring.profiles.active=[dev|test|prod]

# 不同环境配置文件名
application-xxx.properties/yaml/yml
```

### yaml 配置

```yaml

spring:
	profiles:
		active: dev
# 可以通过 --- 来分割模块
---
server:
	port: 8081
spring:
	profiles: dev
---
server:
	port: 8082
spring:
	profiles: test
---
server:
	port: 8083
spring:
	profiles: prod
```



## JSR303校验

```Java
@Validated // 开启
// 检查字段
@Null
@NotNull
@NotBlank
@NotEmpty
@AssertTrue
@AssertFalse
@Size
@Lehgth
@Range
@Email
@Min
@Max
@Past
@Future
@Pattern
// ...
```



## Bootstrap 配置项从哪里来

- 去 META-INF/spring.factories 文件, 找用到的 Configration 类
- 点进去看里边都配置了什么
- 一般会有 Properties 类, 这个类和配置文件的某个前缀进行了绑定
- 这些配置项就是 Bootstrap 配置文件里该前缀可以配置的项目
- 其余需要配置的就是自定义的属性,用于自己导入的Bean的装配

注: 通过配置 debug=true 开启debug模式, 可以看到那些配置类生效了



## 静态资源导入

1. 默认静态资源路径 (优先级从高到底)
   - classpath:/META-INF/resources/
   - classpath/resources/
   - classpath:/static/
   - classpath:/public/
2. 使用 maven 引入 webjar 依赖 (不推荐)
3. 



## 首页配置

静态资源目录下的 index.html



配置首页一般可以用一个 Controller 去重定向一个页面

```java
// 需要模板引擎支持, 访问 template 文件夹 
// template文件夹 只能通过 Controller 访问
// 相当于以前的 WEB-INF
@Controller
public class IndexController {

    @RequestMapping("/")
    String goIndex(){
        return "index";
    }
}
```



## Thymeleaf

https://www.docs4dev.com/docs/zh/thymeleaf/3.0/reference/using_thymeleaf.html

https://www.thymeleaf.org/doc/tutorials/3.0/usingthymeleaf.html

https://developer.aliyun.com/article/769977

```html
<!-- 导入约束 -->
<html lang="en" xmlns:th="http://www.thymeleaf.org">

```

Thymeleaf 语法

- Simple expressions:
  - Variable Expressions: `${...}`
  - Selection Variable Expressions: `*{...}`
  - Message Expressions: `#{...}`
  - Link URL Expressions: `@{...}`
  - Fragment Expressions: `~{...}`
- Literals
  - Text literals: `'one text'`, `'Another one!'`,…
  - Number literals: `0`, `34`, `3.0`, `12.3`,…
  - Boolean literals: `true`, `false`
  - Null literal: `null`
  - Literal tokens: `one`, `sometext`, `main`,…
- Text operations:
  - String concatenation: `+`
  - Literal substitutions: `|The name is ${name}|`
- Arithmetic operations:
  - Binary operators: `+`, `-`, `*`, `/`, `%`
  - Minus sign (unary operator): `-`
- Boolean operations:
  - Binary operators: `and`, `or`
  - Boolean negation (unary operator): `!`, `not`
- Comparisons and equality:
  - Comparators: `>`, `<`, `>=`, `<=` (`gt`, `lt`, `ge`, `le`)
  - Equality operators: `==`, `!=` (`eq`, `ne`)
- Conditional operators:
  - If-then: `(if) ? (then)`
  - If-then-else: `(if) ? (then) : (else)`
  - Default: `(value) ?: (defaultvalue)`
- Special tokens:
  - No-Operation: `_`



示例

```xml
<p th:utext="#{home.welcome}">Welcome to our grocery store!</p>
<p>Today is: <span th:text="${today}">13 february 2011</span></p>

<div th: class="row">
	th:attr 可以用数据替换标签的属性 
    ${{}} 双括号表示 formater 转换
    __${}__表示预处理，一般用不到，国际化有可能会用到
</div>
<form action="subscribe.html" th:action="@{/subscribe}">
    
<hr/>
<div th:text="${msg}"></div>
<div th:utext="${msg}"></div>
<table>
    <td th:each="str:${list}" th:text="${str}"></td>
</table>
```



## 整合 Druid

依赖

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.6</version>
</dependency>
```

配置文件

```yaml
spring:
  datasource:
  # 指定 type 即可
    type: com.alibaba.druid.pool.DruidDataSource
    driver-class-name: com.mysql.cj.jdbc.Driver
    username: root
    password: 
    url: jdbc:mysql://localhost:3307/mybatis?useUnicode=true&characterEncoding=utf-8
```

配置类

```java
@Configuration
public class DruidConfig {

    @Bean
    @ConfigurationProperties(prefix = "spring.datasource")
    public DataSource getDataSource(){
        return new DruidDataSource();
    }

    /**
     * 配置登录 Druid 后台的帐号
     */
    @Bean
    public ServletRegistrationBean addStatViewServlet(){
        ServletRegistrationBean<StatViewServlet> bean = new ServletRegistrationBean<>(new StatViewServlet(),"/druid/*");
        Map<String,String> para = new HashMap<>();
        para.put("loginUsername","root");
        para.put("loginPassword","root");
        para.put("allow","");
        bean.setInitParameters(para);
        return bean;
    }

    /**
     * 为静态资源 和 特殊目录 配置为不被 Druid 统计
     */
    @Bean
    public FilterRegistrationBean addWebStatFilter(){
        FilterRegistrationBean<WebStatFilter> bean = new FilterRegistrationBean<>(new WebStatFilter());
        Map<String,String> para = new HashMap<>();
        para.put("exclusions","*.js,*.css,*.jpg,*.png,/druid/*");
        bean.setInitParameters(para);
        return bean;
    }
}

```



## 整合MyBatis

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>2.2.0</version>
</dependency>

<!-- 另外贴出数据源所需依赖 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
    <scope>runtime</scope>
</dependency>
<!-- druid 数据库连接池 -->
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>druid</artifactId>
    <version>1.2.6</version>
</dependency>
```

==注：事务管理记得在 Service 层加 @Transactional 注解==



## 整合日志

```xml
<!-- 排除默认的 spring-boot-starter-logging -->
<!-- 查看 maven 导包，确定排除成功 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <exclusions>
        <exclusion>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-logging</artifactId>
        </exclusion>
    </exclusions>
</dependency>
<!-- 导入log4j2 对外使用的是 slf4j 门面 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-log4j2</artifactId>
</dependency>
```



## SpringSecurity

依赖导入

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-security</artifactId>
</dependency>
```

使用

```java
//todo
```



## Shiro 

依赖导入

```xml
<dependency>
    <groupId>org.apache.shiro</groupId>
    <artifactId>shiro-spring-boot-starter</artifactId>
    <version>1.8.0</version>
</dependency>
```

核心对象:

- `Subject`
- `SecurityManager`
- `Realm`
- ![image-20211206230753179](D:\MyGitProjectWorkSpace\MyNotebook\Spring\pic\Shiro结构.png)

配置类

```java
@Configuration
public class ShiroConfig {

    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(@Qualifier("shiroSecurityManager") DefaultSecurityManager smanager){
        // 过滤器
        ShiroFilterFactoryBean bean = new ShiroFilterFactoryBean();
        bean.setSecurityManager(smanager);

         /*添加Shiro内置过滤器，常用的有如下过滤器：
        anon： 无需认证就可以访问(没有登录就可以访问)
        authc： 必须认证才可以访问(登录之后才可以访问)
        user： 如果使用了记住我功能就可以直接访问
        perms: 拥有某个资源权限才可以访问
        role： 拥有某个角色权限才可以访问
        */
        Map<String,String> filterMap = new HashMap<>();
        filterMap.put("/","user");
        filterMap.put("/tovip","authc");
        filterMap.put("/index","user");
        filterMap.put("/user/**","perms[user]");
//        filterMap.put("/swagger-resources/**","anon");

        bean.setFilterChainDefinitionMap(filterMap);

        bean.setLoginUrl("/tologin");
        bean.setUnauthorizedUrl("/noauth");

        return bean;
    }

    @Bean(name="shiroSecurityManager")
    public DefaultWebSecurityManager defaultWebSecurityManager(@Qualifier("userRealm") UserRealm realm){
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        manager.setRealm(realm);
        return manager;
    }

    @Bean
    public UserRealm userRealm(){
        return new UserRealm();
    }

}
```

核心工具 Realm

```java
@Slf4j
public class UserRealm extends AuthorizingRealm {

    @Autowired
    private UserMapper userMapper;


    //鉴权
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        log.info("do Authorization 1");
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        Subject user = SecurityUtils.getSubject();
        String parm = (String)user.getPrincipal();
        info.addStringPermission(parm);
        return info;
    }

    //认证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        log.info("do Authentication");


        UsernamePasswordToken token = (UsernamePasswordToken)authenticationToken;
        User user = userMapper.getUserByName(token.getUsername());

        // 帐号不存在
        if(user == null || !token.getUsername().equals(user.getName())){
            return null;
        }

        return new SimpleAuthenticationInfo(user.getParm(),user.getPwd(),"root");
    }
}
```



## 异步方法

==在方法上加 @Async==

==在启动类上加@EnableAsyn==

## 定时任务

==在方法上加 @Scheduled(cron="[expression]")==

==在启动类上加 @EnableScheduling==

cron="[expression]" 去百度



## Jedis

### redis服务器配置

```
# 绑定redis实例接收那些网卡地址发来的请求
# 需要把服务器网卡地址也绑定上,否则外网连不上redis
bind 127.0.0.1 和服务器网卡ip 使用 ip address 查看

# 设置密码
requirepass password 

# 设置可后台运行
daemonize yes

# 另外记得配置服务器的 防火墙 开启 redis 端口
# 允许 客户端 IP 访问(有密码可以忽略.但是推荐开启ip过滤,系统级隔绝非受信客户端ip的链接)
```



### 依赖

```xml
<dependency>
    <groupId>redis.clients</groupId>
    <artifactId>jedis</artifactId>
    <version>3.7.0</version>
</dependency>
<!-- 需要slf4j实现类,这里选用log4j2 -->
<dependency>    
    <groupId>org.apache.logging.log4j</groupId>
    <artifactId>log4j-slf4j-impl</artifactId>
    <version>2.14.1</version>
</dependency>
```

### 链接

```java
public class JedisTest {
    public static void main(String[] args) {
        // host & port
        Jedis jedis = new Jedis("42.193.125.192",6379);
        // password if exists
        jedis.auth("xxxx");
        System.out.println(jedis.ping());
        jedis.set("key","1");
        Integer key = Integer.valueOf(jedis.get("key"));
        System.out.println(key);
        jedis.close();
    }
}
```

### 事务

```java
public class JedisTest {
    public static void main(String[] args) {
        Jedis jedis = new Jedis("42.193.125.192",6379);
        jedis.auth("francis");
        JSONObject user = new JSONObject();
        user.put("name","francis");
        user.put("age","18");
        System.out.println(jedis.ping());

        Transaction multi = jedis.multi();
        try {
            multi.set("user1", user.toJSONString());
            multi.set("user2", user.toJSONString());
            multi.get("user1");
            multi.get("user2");
            multi.exec();
        }catch (Exception e){
            multi.discard();
            e.printStackTrace();
        }finally {
            jedis.close();
        }
    }
}
```

## SpringBoot 整合 Redis

底层采用 lettuce, 使用 netty, 实例可以在线程中共享,减少线程数量,性能更好,也更安全.

原理来的 Jedis 使用连接池, 性能和安全性 没有 lettuce 好

### 配置

```yaml
spring:
  redis:
    host: 42.193.125.192
    port: 6379
    password: francis
    lettuce:
      pool:
        max-active: 2
        enabled: true
```

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
</dependency>
<!-- 使用链接池的场景需要额外引入下面配置 -->
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-pool2</artifactId>
</dependency>
```



### 使用

```java
@Autowired
//    private RedisTemplate redisTemplate;
private RedisTemplate<String,String> redisTemplate; // 相当于 StringRedisTemplate
//    @Autowired
//    private StringRedisTemplate stringRedisTemplate;
@Test
void redisTest(){

    redisTemplate.opsForValue().set("key","value");
    redisTemplate.opsForValue().get("key");
    redisTemplate.opsForList().leftPush("list","1");
    redisTemplate.opsForList().rightPush("list","2");
    redisTemplate.opsForHash().put("hash","hkey","hvalue");

    SessionCallback<Object> callback = new SessionCallback<Object>() {
        @Override
        public Object execute(RedisOperations operations) throws DataAccessException {
            operations.multi();
            // 自动配置里支持中文
            operations.opsForValue().set("name", "francis.赵");
            operations.opsForValue().set("age", "18");
            operations.opsForValue().get("name");
            operations.opsForValue().get("age");
            return operations.exec();
        }
    };
    logger.info(redisTemplate.execute(callback).toString());

    RedisConnection connection1 = Objects.requireNonNull(redisTemplate.getConnectionFactory()).getConnection();
    RedisConnection connection2 = RedisConnectionUtils.getConnection(redisTemplate.getConnectionFactory());
    Boolean isTransactional = RedisConnectionUtils.isConnectionTransactional(connection1,redisTemplate.getConnectionFactory());
    logger.info("connection1: " + connection1);
    logger.info("connection2: " + connection2);
    logger.info("connection1 == connection1 is: " + (connection1 == connection2));
    logger.info("isTransactional: " + isTransactional);

    connection1.flushDb();
    connection1.close();
    connection2.close();
}
```

### 配置 json 序列化方式

```java
@Configuration
public class RedisConfig {

    /**
     * 自定义 RedisTemplate 主要配置了序列化方式
     * @param redisConnectionFactory redisConnectionFactory
     * @return RedisTemplate<String, Object>
     */
    @Bean(name="redisTemplate")
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);


        Jackson2JsonRedisSerializer<Object> jsonSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        ObjectMapper om = new ObjectMapper();
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        om.activateDefaultTyping(new LaissezFaireSubTypeValidator(),ObjectMapper.DefaultTyping.NON_FINAL);
//        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        jsonSerializer.setObjectMapper(om);

        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        template.setKeySerializer(stringRedisSerializer);
        template.setHashKeySerializer(stringRedisSerializer);
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        return template;
    }
}
```



## Dubbo
主要解决 RPC 远程过程调用的问题

核心: 网络通讯 + 序列化

![image-20211210052650528](D:\MyGitProjectWorkSpace\MyNotebook\Spring\pic\dubbo工作流程.png)



## Zookepper

分布式协调程序, 提供配置维护, 域名维护, 分布式同步, 组服务

和 Dubbo 搭配的服务发现/服务注册中心



Spring boot + zookeeper + dubbo

业务实现框架 --  服务注册中心 -- RPC 远程过程调用工具

这就是分布式系统的一种解决方案, 相比于微服务架构,还缺少的组件有 网关/路由 和 断路器(熔断限流降级机制)



### 第一代微服务架构

Netflix 系

Zuul : 网关, 路由

Eureka : 服务发现与注册

Hystrix: 断路器 --> 熔断降级

Feigin: 服务调用(基于Http)

Ribbon: 客户端负载均衡

Archaius: 分布式配置



### 新一代微服务架构

Spring cloud alibaba

网关 : Gateway

服务发现与注册中心: Nacos

服务调用: Dubbo

断路器: Sentinel

负载均衡: Dubbo LB

分布式配置: Nacos

分布式事务: Seata
