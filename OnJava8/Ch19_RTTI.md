## CH19 RunTime Type Information，运行时类型信息  -- 反射机制

### .class文件 与 类的加载
类的类型信息存储在 .class 文件中， 由类加载器 classloader 在程序运行时加载到JVM。
加载时机为：
- 第一次引用类静态成员时， 构造函数实际上也是静态static的，只是使用 new 关键字调用，所以构造类的实例属于对静态成员的引用。
- Class.for("className") 返回类的Class对象，此时也会触发对类的加载。

### Class对象
Class对象是存储类的所有类型信息的java对象，获取Class对象的两种方式：
1. Class.forName("className");
2. ClassName.class;
   
### 类加载过程
1. 加载：从.class文件或其他位置读取字节码，构造Class对象
2. 链接：验证字节码，并未static字段分配存储空间
   1. 验证： 文件格式验证，元数据验证，字节码验证，符号引用验证。
   2. 准备： 为static字段分配存储空间（方法区），注：实例变量和对象是后续在堆中分配的
   3. 解析： 将常量池中的符号引用替换为直接引用的过程，类或接口的解析，字段解析，类方法解析。
3. 初始化：初始化超类， 为 static 字段赋值。

### classloader 类加载器
3种加载器：
1. Bootstrap ClassLoader 启动类加载器： 
   JVM的一部分， C++实现，负责将<JAVA_HOME>/lib路径下的核心类库或-Xbootclasspath参数指定的路径下的jar包加载到内存中，根据名字加载，只加载包名为java、javax、sun等开头的类
2. Extension ClassLoader 拓展类加载器： 
   sun.misc.Launcher$ExtClassLoader类， Java实现，Launcher的静态内部类，负责加载<JAVA_HOME>/lib/ext目录下或者由系统变量-Djava.ext.dir指定位路径中的类库。
3. System ClassLoader 系统类加载器：
   sun.misc.Launcher$AppClassLoader。负责加载系统类路径java -classpath或-D java.class.path 指定路径下的类库，即classpath路径，一般情况下该类加载是程序中默认的类加载器，通过ClassLoader#getSystemClassLoader()方法可以获取到该类加载器。

### 动态代理
```DaynamicProxy``` 实现 ```InvocationHandler``` 接口，重写 ``` public Object invoke(Object proxy, Method method,Object[] args) throwsThrowable ``` 方法，
将被代理的 ```Object``` 传给 ```DaynamicProxy```, 将 ```DaynamicProxy``` 传给 


```java
SomeInterface proxyed = (SomeInterface) Proxy.newProxyInstance(
   SomeInterface.class.getClassLoader(), // 要使用的ClassLoader
   new Class[]{SomeInterface.class}, // 要被代理的接口
   new DynamicProxyHandler(real) ); // 代理的 handler,并将被代理的 Object 传给代理handler
// 将返回转换为被代理的接口,方便调用

```