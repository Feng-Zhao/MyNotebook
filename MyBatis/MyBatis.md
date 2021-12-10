[TOC]



# MyBatis

主要优点:

- sql 和 代码分离

- 支持对象和集合映射

- 支持动态sql



## 配置文件

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <properties>
        <property name="driver" value="com.mysql.cj.jdbc.Driver"/>
        <property name="url" value="jdbc:mysql://localhost:3307/mybatis?useSSL=true&amp;useUnicode=true&amp;characterEncoding=UTF-8&amp;serverTimezone=Asia/Shanghai"/>
        <property name="username" value="root"/>
        <property name="password" value=""/>
    </properties>

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
    </mappers>
</configuration>
```



## Java配置方式(只读)

```java
DataSource dataSource = BlogDataSourceFactory.getBlogDataSource();
TransactionFactory transactionFactory = new JdbcTransactionFactory();
Environment environment = new Environment("development", transactionFactory, dataSource);
Configuration configuration = new Configuration(environment);
configuration.addMapper(BlogMapper.class);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(configuration);
```



## 获取SqlSessionFactory

```java
String resource = "/mybatis-config.xml";
InputStream inputStream = Resources.getResourceAsStream(resource);
SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
```

简化: 应为 SqlSessionFactory 编写工具类, 利用单例获取

```java
package util;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyBatisUtil {
    private static String resource;
    private volatile static SqlSessionFactory sqlSessionFactory;

    // 双锁 + volatile 单例 此处 volatile 阻止指令重排序,保证写内存之前不会有读内存操作
    public static SqlSessionFactory getSqlSessionFactory() throws IOException {
        if(sqlSessionFactory == null){
            synchronized (MyBatisUtil.class) {
                if(sqlSessionFactory == null) {
                    Properties properties = new Properties();
                    InputStream propIns = Resources.getResourceAsStream("mybatis.properties");
//                    InputStream propIns = MyBatisUtil.class.getResourceAsStream("/mybatis.properties");

//                    ClassPathResource classPathResource = new ClassPathResource("mybatis.properties");
//                    InputStream propIns = classPathResource.getInputStream();
                    properties.load(propIns);
                    resource = properties.getProperty("configLocation");
                    InputStream ins = Resources.getResourceAsStream(resource);
                    sqlSessionFactory = new SqlSessionFactoryBuilder().build(ins);
                    propIns.close();
                    // SqlSessionFactoryBuilder 关闭了流
//                    ins.close();
                }
            }
        }
        return sqlSessionFactory;
    }

    private MyBatisUtil(){

    }

    public static SqlSession getSqlSession() throws IOException {
        return getSqlSessionFactory().openSession();
    }
}

/*
// 静态内部类加载 实现单例, 静态内部类只有在 调用 getInstance() 才初始化
// 即保证线程安全,又实现懒加载
public class SingletonDemo {
    // 私有静态内部类 保证 单例+线程安全+懒加载
    private static class SingletonHolder{
        private static final SingletonDemo instance=new SingletonDemo();
    }
    // 私有构造器,保证不被实例化
    private SingletonDemo(){
        System.out.println("Singleton has loaded");
    }

    public static SingletonDemo getInstance(){
        return SingletonHolder.instance;
    }
}
 */

/*
// Effective Java 作者 Josh Bloch 推荐
// 绝对无法实例化,单例,线程安全,懒加载
enum SingletonDemo{
    INSTANCE;
    public void otherMethods(){
        System.out.println("Something");
    }
}
// 调用时
SingletonDemo.INSTANCE.otherMethods();
 */

```



使用 properties 文件 配置 mybatis-config.xml 的文件路径 

mybatis.properties 文件路径 

src/main/resources/mybatis.properties

```properties
configLocation=mybatis-config.xml
```



## Mapper 编写

```java
public interface UserMapper {
    List<User> selectAll();
    User selectById(int id);
}
```

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE mapper
        PUBLIC "-//mybatis.org//DTD Mapper 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-mapper.dtd">
<mapper namespace="mapper.UserMapper">
    <select id="selectAll" resultType="pojo.User">
        select * from user
    </select>

    <select id="selectById" resultType="pojo.User">
        select * from user where id = #{id}
    </select>
</mapper>
```

`mybatis-config.xml` 文件中添加 Mapper 注册

```xml
<mappers>
        <mapper resource="mapper/UserMapper.xml"/>
</mappers>
```



**注1: mapper.xml 文件中的 namespace 使用 . 分隔符, mapper 注册时 使用 / 分隔符**

**注2 : 注意Maven的静态资源过滤:**

`pom` 文件中添加

```xml
<build>
        <resources>
            <resource>
                <directory>src/main/java</directory>
                <includes>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
            <resource>
                <directory>src/main/resources</directory>
                <includes>
                    <include>**/*.properties</include>
                    <include>**/*.xml</include>
                </includes>
                <filtering>false</filtering>
            </resource>
        </resources>
    </build>
```



## 语句执行

```java
@Test
public void sqlTest(){
    int id = 1;
    try(SqlSession sqlSession = MyBatisUtil.getSqlSession()){
        UserMapper mapper = sqlSession.getMapper(UserMapper.class);

        System.out.println("===== selectById ======");
        User user = mapper.selectById(id);
        System.out.println(user.toString());

        System.out.println("===== selectAll ======");
        List<User> list = mapper.selectAll();
        for (User u: list
            ) {
            System.out.println(u);
        }

        System.out.println("===== insertUser ======");
        mapper.insertUser(new User(100,"test100","100"));
        user = mapper.selectById(100);
        System.out.println(user);

        System.out.println("===== updateUser ======");
        mapper.updateUser(new User(100,"Modified","Modified"));
        user = mapper.selectById(100);
        System.out.println(user);

        System.out.println("===== deleteUser ======");
        mapper.deleteUser(100);
        list = mapper.selectAll();
        for (User u: list
            ) {
            System.out.println(u);
        }

        sqlSession.commit();
    }catch (IOException e){
        e.printStackTrace();
    }
}
```

**注: 增删改 注意提交事务 sqlSession.commit();**



## MyBatis 属性配置

https://mybatis.net.cn/configuration.html#settings 

![image-20211201144314042](D:\MyGitProjectWorkSpace\MyNotebook\Properties类使用\pic\mybatis属性配置.png)

Settings:

```xml
<settings>
        <!-- mapper缓存 全局开关-->
        <setting name="cacheEnabled" value="true"/>
        <!-- 懒加载-->
        <setting name="lazyLoadingEnabled" value="true"/>
        <setting name="multipleResultSetsEnabled" value="true"/>
        <setting name="useColumnLabel" value="true"/>
        <!-- 允许 JDBC 支持自动生成主键-->
        <setting name="useGeneratedKeys" value="false"/>
        <!-- 指定 MyBatis 应如何自动映射列到字段或属性。
        NONE 表示关闭自动映射；
        PARTIAL 只会自动映射没有定义嵌套结果映射的字段。
        FULL 会自动映射任何复杂的结果集（无论是否嵌套）-->
        <setting name="autoMappingBehavior" value="PARTIAL"/>
        <!-- 指定发现自动映射目标未知列（或未知属性类型）的行为。
        NONE: 不做任何反应
        WARNING: 输出警告日志（'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior' 的日志等级必须设置为 WARN）
        FAILING: 映射失败 (抛出 SqlSessionException)-->
        <setting name="autoMappingUnknownColumnBehavior" value="WARNING"/>
        <!-- 配置默认的执行器。
        SIMPLE 就是普通的执行器；
        REUSE 执行器会重用预处理语句（PreparedStatement）；
        BATCH 执行器不仅重用语句还会执行批量更新。-->
        <setting name="defaultExecutorType" value="SIMPLE"/>
        <!-- 设置超时时间 秒 -->
        <setting name="defaultStatementTimeout" value="25"/>
        <setting name="defaultFetchSize" value="100"/>
        <setting name="safeRowBoundsEnabled" value="false"/>
        <!-- 启驼峰命名自动映射-->
        <setting name="mapUnderscoreToCamelCase" value="false"/>
        <!-- 本地缓存机制-->
        <setting name="localCacheScope" value="SESSION"/>
        <setting name="jdbcTypeForNull" value="OTHER"/>
        <setting name="lazyLoadTriggerMethods" value="equals,clone,hashCode,toString"/>
    </settings>
```



### 环境配置

```xml
<!-- default 指定默认环境,建立SqlSessionFactory 时,也可以显式指定环境 -->
<environments default="development">
        <environment id="development">
            <!-- 事务管理器 两种类型 JDBC / MANAGED]-->
            <!-- MANAGED 类型为交给容器管理,但是默认关闭链接,不需要的情况时,可配置更改 -->
            <transactionManager type="JDBC"/>
            <!-- 数据源 type="[UNPOOLED|POOLED|JNDI]" -->
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>
```



### 别名设置

#### 设置别名,简化 Mapper.xml 书写

```xml
<!-- 单独设置-->
<typeAliases>
  <typeAlias alias="Author" type="domain.blog.Author"/>
  <typeAlias alias="Blog" type="domain.blog.Blog"/>
  <typeAlias alias="Comment" type="domain.blog.Comment"/>
  <typeAlias alias="Post" type="domain.blog.Post"/>
  <typeAlias alias="Section" type="domain.blog.Section"/>
  <typeAlias alias="Tag" type="domain.blog.Tag"/>
</typeAliases>

<!-- 扫面描包 -->
<typeAliases>
  <package name="domain.blog"/>
</typeAliases>

<!-- 
mapper.xml 里使用小写
<select id="selectAll" resultType="user">
        select * from user
    </select>
-->
```

#### 在实体类里用注解设置别名

```java
// 注:使用注解时, 优先注解设置的别名, 配置文件里的别名失效
@Alias("book")
public class Book {
    private int id;
    private String name;
    private String author;
}
```



## resultMap 结果映射

```xml
    <resultMap id="bookMap" type="Book">
        <!-- 设置 列名 和 对应的 实体类属性名 -->
        <result column="author" property="authorName"/>
    </resultMap>

	<!-- 使用 resultMap, 不用 resultType -->
    <select id="queryByName" resultMap="bookMap">
        select * from book where name like "%"#{name}"%";
    </select>
```



## 分页

### pagehelper 推荐

https://pagehelper.github.io/

### 传 limit

### 工具类

- pagehelper
- rowBounds (逻辑分页, 不推荐)



## MyBatis 执行过程



- Resource 读取 配置文件
- SqlSessionFactoryBuilder 生成 SqlSessionFactory
- (加载事务管理器)
- 创建 executor
- SqlSessionFactory 构造 SqlSession
- SqlSession 生成 Mapper
- Mapper 调用 SqlSession 中的 Executor 执行sql

##  



## 注解形式写 sql

```java
@Select("select id,name from book where id = #{id}")
Book selectById(@Param("id") int id);
```



## 复杂关系映射处理

建表语句

```sql
create table mybatis.student
(
    id   int         not null primary key,
    name varchar(64) null,
    tid  int         null,
    constraint tid
        foreign key (tid) references mybatis.teacher (id)
);
create table mybatis.teacher
(
    id   int         not null
        primary key,
    name varchar(64) null
);

```



### 多对一

```java
public class Student {
    private int id;
    private String name;
    private Teacher teacher;
}
public class Teacher {
    private int id;
    private String name;
}
```

#### 子查询形式处理

```xml
<mapper namespace="mapper.StudentMapper">

    <resultMap id="StudentTeacher" type="student">
        <!-- association 对象 关联关系 -->
        <!-- collection 集合 关联关系 -->
        <association property="teacher" column="tid"
                     javaType="teacher" select="getTeacherById">
            <!-- 上方的 select="getTeacherById" 表示
            用 column 标识的值再去调用 另一个 select 语句-->
        </association>
    </resultMap>

    <select id="getAll" resultMap="StudentTeacher">
        select s.id, s.name, s.tid
        from student s
    </select>

    <select id="getTeacherById" resultType="teacher">
        select * from teacher where id = #{tid}
    </select>

</mapper>
```

#### 结果嵌套处理

```xml
<resultMap id="ST2" type="student">
    <result property="id" column="sid"/>
    <result property="name" column="sname"/>
    <association property="teacher" javaType="Teacher">
        <result property="id" column="tid"/>
        <result property="name" column="tname"/>
    </association>
</resultMap>
<!-- 此处多表查询必须用别名 直接 s.id, s.name 上边识别不出来 -->
<select id="getAll2" resultMap="ST2">
    select
    s.id sid,s.name sname,s.tid,t.id tid,t.name tname
    from student s,teacher t where s.tid = t.id;
</select>
```



### 一对多

```java
public class Student2 {
    private int id;
    private String name;
    private int tid;
}
public class Teacher {
    private int id;
    private String name;

    List<Student2> studentList;
}
```



```xml
    <!-- 按结果集匹配 -->
    <select id="getFullTeacher" resultMap="fullTeacher">
        select
        s.id sid, s.name sname, s.tid stid, t.id tid, t.name tname
        from student s, teacher t
        where s.tid = t.id and t.id = #{tid}
    </select>
    <resultMap id="fullTeacher" type="teacher">
        <result property="id" column="tid"/>
        <result property="name" column="tname"/>
        <collection property="studentList" ofType="student2">
            <result property="id" column="sid"/>
            <result property="name" column="sname"/>
            <result property="tid" column="stid"/>
        </collection>
    </resultMap>

    <!-- 按子查询匹配 -->
    <select id="getFullTeacher2" resultMap="fullTeacher2">
        select
            id, name, id tid
        from teacher t
        where t.id = #{tid};
    </select>
    <resultMap id="fullTeacher2" type="teacher">
        <collection property="studentList" javaType="java.util.ArrayList"
                    ofType="student" select="getStudentByTeacher" column="tid">
        </collection>
    </resultMap>
    <select id="getStudentByTeacher" resultType="student2">
        select * from student where tid = #{tid};
    </select>
```



### 多对多

**需要一张中间表, 完成两个一对多关系**

https://blog.csdn.net/dwenxue/article/details/82108178





## 动态SQL

### if

```xml
<select id="findActiveBlogWithTitleLike"
     resultType="Blog">
  SELECT * FROM BLOG
  WHERE state = ‘ACTIVE’
  <if test="title != null">
    AND title like #{title}
  </if>
</select>
```

### choose、when、otherwise

[sql 里的 case when then]

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG WHERE state = ‘ACTIVE’
  <choose>
    <when test="title != null">
      AND title like #{title}
    </when>
    <when test="author != null and author.name != null">
      AND author_name like #{author.name}
    </when>
    <otherwise>
      AND featured = 1
    </otherwise>
  </choose>
</select>
```

### where 

子句有匹配时插入 WHERE， 并且 如果第一个子句 以 AND / OR 开头， 抹去开头

```xml
<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
  <where>
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
  </where>
</select>
```

 ### set

用于动态更新

*set* 标签会动态地在行首插入 SET 关键字，并会删掉额外的逗号

```xml
<update id="updateAuthorIfNecessary">
  update Author
    <set>
      <if test="username != null">username=#{username},</if>
      <if test="password != null">password=#{password},</if>
      <if test="email != null">email=#{email},</if>
      <if test="bio != null">bio=#{bio}</if>
    </set>
  where id=#{id}
</update>
```

### trim

*where* 标签不满足需求时 可以用 *trim* 标签 自定义 需要抹除的前缀

```
<!-- 和 where 标签等价的自定义 trim  -->
<trim prefix="WHERE" prefixOverrides="AND |OR ">
  ...
</trim>

<!-- 与 set 元素等价的自定义 trim -->
<trim prefix="SET" suffixOverrides=",">
  ...
</trim>
```



### foreach

用于集合遍历，主要是 IN 条件语句



允许你指定一个集合*collection="list"*，声明可以在元素体内使用的集合项（item）和索引（index）变量。它也允许你指定开头与结尾的字符串以及集合项迭代之间的分隔符。

> open="(" separator="," close=")"

允许将任何可迭代对象（如 List、Set 等）、Map 对象或者数组对象作为集合参数传递给 *foreach*。

当使用可迭代对象或者数组时，index 是当前迭代的序号，item 的值是本次迭代获取到的元素。当使用 Map 对象（或者 Map.Entry 对象的集合）时，index 是键，item 是值。

```xml
<select id="selectPostIn" resultType="domain.blog.Post">
  SELECT *
  FROM POST P
  WHERE ID in
  <foreach item="item" index="index" collection="list"
      open="(" separator="," close=")">
        #{item}
  </foreach>
</select>
```



### sql片段引用

<sql> 标签定义 <include refid="subsql"></include> 引用

```xml
<sql id="subsql">
    <if test="state != null">
         state = #{state}
    </if>
    <if test="title != null">
        AND title like #{title}
    </if>
    <if test="author != null and author.name != null">
        AND author_name like #{author.name}
    </if>
</sql>

<select id="findActiveBlogLike"
     resultType="Blog">
  SELECT * FROM BLOG
    <where>
  		<include refid="subsql"></include>
    </where>
</select>
```

注： 

- sql片段 最好基于单表 方便服用

- 不要把 <where> 放在片段里



## 缓存

MyBatis 有两级缓存

### 一级缓存（本地缓存）

一级缓存是基于 *sqlSession* 的， 默认开启

一级缓存底层是一个 HashMap

==**注：增删改  insert/delete/update 语句  会整体刷新缓存， 即使增删改的数据与缓存中的数据完全无关，也会刷新缓存**==

### 二级缓存

二级缓存是基于 *namespace* 即 *Mapper* 的

另外有 Cache 接口，可以自定义二级缓存

开启方式: Mapper.xml 文件里加入 <cache> 标签

> - 映射语句文件中的所有 select 语句的结果将会被缓存。
> - 映射语句文件中的所有 insert、update 和 delete 语句会刷新缓存。
> - 缓存会使用最近最少使用算法（LRU, Least Recently Used）算法来清除不需要的缓存。
> - 缓存不会定时进行刷新（也就是说，没有刷新间隔）。
> - 缓存会保存列表或对象（无论查询方法返回哪种）的 1024 个引用。
> - 缓存会被视为读/写缓存，这意味着获取到的对象并不是共享的，可以安全地被调用者修改，而不干扰其他调用者或线程所做的潜在修改。

```xml
<!-- Mapper.xml 中开启二级缓存 -->
<cache/>
	<!-- 另外可以配置更多参数 -->
	<cache
        <!-- 指定策略 -->
        eviction="FIFO" 
        <!-- 刷新间隔，毫秒 -->
        flushInterval="60000" 
        <!-- 缓存大小 -->
        size="512" 
        <!-- 返回对象只读 ,只读会转存同一个对象的引用,读写模式会通过序列化,生成新的对象实例-->
        readOnly="true">
	</cache>

<!-- 另外需要确认 配置文件中 开启全局缓存开关 -->
<setting name="cacheEnabled" value="true"/>
```

策略：

>- `LRU` – 最近最少使用：移除最长时间不被使用的对象。
>- `FIFO` – 先进先出：按对象进入缓存的顺序来移除它们。
>- `SOFT` – 软引用：基于垃圾回收器状态和软引用规则移除对象。
>- `WEAK` – 弱引用：更积极地基于垃圾收集器状态和弱引用规则移除对象。



工作机制:

会话关闭,一级缓存放入二级缓存,之后清空一级缓存,之后的会话可以从二级缓存中查找

不同 Mapper 查询的数据是隔离的,会保存在自己的缓存 Map 中



**注： 二级缓存是事务性的。这意味着，当 SqlSession 完成并提交时，或是完成并回滚，但没有执行 flushCache=true 的 insert/delete/update 语句时，缓存会获得更新。**

**注: 二级缓存需要 实体类 能够序列化( implements Serializable)**

**注: MyBatis 支持第三方 缓存, 例如 EhCache**

自定义缓存配置方式

> ```xml
> <!-- 自定义 -->
> <cache type="com.domain.something.MyCustomCache"/>
> <!-- EhCache -->
> <cache type="org.mybatis.caches.ehcache.EhCacheCache"/>
> ```



### 缓存查询顺序

1. 二级缓存
2. 一级缓存
3. 数据库



## 附录

### 最终配置文件

#### *mybatis-config.xml*

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<!DOCTYPE configuration
        PUBLIC "-//mybatis.org//DTD Config 3.0//EN"
        "http://mybatis.org/dtd/mybatis-3-config.dtd">
<configuration>
    <!-- properties 优先使用外部配置文件 -->
    <properties resource="datasource.properties">
    </properties>

<!--    <settings>-->
<!--        &lt;!&ndash; mapper缓存 全局开关&ndash;&gt;-->
<!--        <setting name="cacheEnabled" value="true"/>-->
<!--        &lt;!&ndash; 懒加载&ndash;&gt;-->
<!--        <setting name="lazyLoadingEnabled" value="true"/>-->
<!--        <setting name="multipleResultSetsEnabled" value="true"/>-->
<!--        <setting name="useColumnLabel" value="true"/>-->
<!--        &lt;!&ndash; 允许 JDBC 支持自动生成主键&ndash;&gt;-->
<!--        <setting name="useGeneratedKeys" value="false"/>-->
<!--        &lt;!&ndash; 指定 MyBatis 应如何自动映射列到字段或属性。-->
<!--        NONE 表示关闭自动映射；-->
<!--        PARTIAL 只会自动映射没有定义嵌套结果映射的字段。-->
<!--        FULL 会自动映射任何复杂的结果集（无论是否嵌套）&ndash;&gt;-->
<!--        <setting name="autoMappingBehavior" value="PARTIAL"/>-->
<!--        &lt;!&ndash; 指定发现自动映射目标未知列（或未知属性类型）的行为。-->
<!--        NONE: 不做任何反应-->
<!--        WARNING: 输出警告日志（'org.apache.ibatis.session.AutoMappingUnknownColumnBehavior' 的日志等级必须设置为 WARN）-->
<!--        FAILING: 映射失败 (抛出 SqlSessionException)&ndash;&gt;-->
<!--        <setting name="autoMappingUnknownColumnBehavior" value="WARNING"/>-->
<!--        &lt;!&ndash; 配置默认的执行器。-->
<!--        SIMPLE 就是普通的执行器；-->
<!--        REUSE 执行器会重用预处理语句（PreparedStatement）；-->
<!--        BATCH 执行器不仅重用语句还会执行批量更新。&ndash;&gt;-->
<!--        <setting name="defaultExecutorType" value="SIMPLE"/>-->
<!--        &lt;!&ndash; 设置超时时间 秒 &ndash;&gt;-->
<!--        <setting name="defaultStatementTimeout" value="25"/>-->
<!--        <setting name="defaultFetchSize" value="100"/>-->
<!--        <setting name="safeRowBoundsEnabled" value="false"/>-->
<!--        &lt;!&ndash; 启驼峰命名自动映射&ndash;&gt;-->
<!--        <setting name="mapUnderscoreToCamelCase" value="false"/>-->
<!--        &lt;!&ndash; 本地缓存机制&ndash;&gt;-->
<!--        <setting name="localCacheScope" value="SESSION"/>-->
<!--        <setting name="jdbcTypeForNull" value="OTHER"/>-->
<!--        <setting name="lazyLoadTriggerMethods" value="equals,clone,hashCode,toString"/>-->
<!--    </settings>-->
    
    <settings>
        <setting name="logImpl" value="SLF4J"/>
        <setting name="mapUnderscoreToCamelCase" value="true"/>
    </settings>

    <typeAliases>
        <package name="pojo"/>
    </typeAliases>

    <environments default="development">
        <environment id="development">
            <transactionManager type="JDBC"/>
            <dataSource type="POOLED">
                <property name="driver" value="${driver}"/>
                <property name="url" value="${url}"/>
                <property name="username" value="${username}"/>
                <property name="password" value="${password}"/>
            </dataSource>
        </environment>
    </environments>

    <mappers>
        <mapper resource="mapper/UserMapper.xml"/>
    </mappers>

</configuration>
```

#### *mybatis.properties*

```xml
configLocation=mybatis-config.xml
```

#### datasource.properties

```properties
driver=com.mysql.cj.jdbc.Driver
url=jdbc:mysql://localhost:3307/mybatis?useSSL=true&useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
username=root
password=
```

#### *MyBatisUtil.java*

```java
package util;


import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class MyBatisUtil {
    private static String resource;
    private volatile static SqlSessionFactory sqlSessionFactory;

    // 双锁 + volatile 单例 此处 volatile 阻止指令重排序,保证写内存之前不会有读内存操作
    public static SqlSessionFactory getSqlSessionFactory() throws IOException {
        if(sqlSessionFactory == null){
            synchronized (MyBatisUtil.class) {
                if(sqlSessionFactory == null) {
                    Properties properties = new Properties();
                    InputStream propIns = Resources.getResourceAsStream("mybatis.properties");
//                    InputStream propIns = MyBatisUtil.class.getResourceAsStream("/mybatis.properties");

//                    ClassPathResource classPathResource = new ClassPathResource("mybatis.properties");
//                    InputStream propIns = classPathResource.getInputStream();
                    properties.load(propIns);
                    resource = properties.getProperty("configLocation");
                    InputStream ins = Resources.getResourceAsStream(resource);
                    sqlSessionFactory = new SqlSessionFactoryBuilder().build(ins);
                    propIns.close();
                    // SqlSessionFactoryBuilder 关闭了流
//                    ins.close();
                }
            }
        }
        return sqlSessionFactory;
    }

    private MyBatisUtil(){

    }

    public static SqlSession getSqlSession() throws IOException {
        return getSqlSessionFactory().openSession();
    }
}

/*
// 静态内部类加载 实现单例, 静态内部类只有在 调用 getInstance() 才初始化
// 即保证线程安全,又实现懒加载
public class SingletonDemo {
    // 私有静态内部类 保证 单例+线程安全+懒加载
    private static class SingletonHolder{
        private static final SingletonDemo instance=new SingletonDemo();
    }
    // 私有构造器,保证不被实例化
    private SingletonDemo(){
        System.out.println("Singleton has loaded");
    }

    public static SingletonDemo getInstance(){
        return SingletonHolder.instance;
    }
}
 */

/*
// Effective Java 作者 Josh Bloch 推荐
// 绝对无法实例化,单例,线程安全,懒加载
enum SingletonDemo{
    INSTANCE;
    public void otherMethods(){
        System.out.println("Something");
    }
}
// 调用时
SingletonDemo.INSTANCE.otherMethods();
 */

```

#### *UUIDUtil*

```java
package util;

import java.util.UUID;

public class UUIDUtil {
    public static String getUUID(){
        return UUID.randomUUID().toString().replace("-","");
    }
}
```

