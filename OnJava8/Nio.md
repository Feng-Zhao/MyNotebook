[toc]
## 基础IO

基础 IO 总体来说分为两类 一类处理字节流 [`InputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html) and [`OutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html), 一类处理字符流 [`Reader`](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html) and [`Writer`](https://docs.oracle.com/javase/8/docs/api/java/io/Writer.html)

### Byte Streams

基础的接口 字节类 IO流 [`InputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/InputStream.html) and [`OutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/OutputStream.html)

### Character Streams

基础接口 字符类 [`Reader`](https://docs.oracle.com/javase/8/docs/api/java/io/Reader.html) and [`Writer`](https://docs.oracle.com/javase/8/docs/api/java/io/Writer.html)

### Buffered Streams 一次处理 '一行' 数据

[`BufferedInputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedInputStream.html) and [`BufferedOutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedOutputStream.html) 

[`BufferedReader`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedReader.html) and [`BufferedWriter`](https://docs.oracle.com/javase/8/docs/api/java/io/BufferedWriter.html)

### Scanning and Formatting 更抽象的数据处理

[`Scanner`](https://docs.oracle.com/javase/8/docs/api/java/util/Scanner.html) 带数据类型的读取 使用 nextXXX() 读取需要的数据类型

[`PrintWriter`](https://docs.oracle.com/javase/8/docs/api/java/io/PrintWriter.html),  [`PrintStream`](https://docs.oracle.com/javase/8/docs/api/java/io/PrintStream.html), 格式化输出 

示例

> ```
> System.out.format("The square root of %d is %f.%n", i, r);
> // 匹配类型
> 
> 整形数字	d formats an integer value as a decimal value.
> 浮点型数字  f formats a floating point value as a decimal value.
> 结束符		n outputs a platform-specific line terminator.
> 
> 
> 十六进制 	x formats an integer as a hexadecimal value.
> 字符串 	s formats any value as a string.
> ```

![Elements of a format specifier](.\pic\formater.gif)

### Data Stream 支持基本数据类型和String

- [`DataInput`](https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html) and [`DataOutput`](https://docs.oracle.com/javase/8/docs/api/java/io/DataOutput.html) 接口

  - 实现类 [`DataInputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/DataInputStream.html) and [`DataOutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/DataOutputStream.html). 注： Data Stream 使用捕捉 [`EOFException`](https://docs.oracle.com/javase/8/docs/api/java/io/EOFException.html) 的方式感知文件结尾。另外 Data Stream 不适合用于金融方面的数字， 因为不支持 [`BigDecimal`](https://docs.oracle.com/javase/8/docs/api/java/math/BigDecimal.html)。金融方面的数字可以使用 object stream

  - ```java
    //使用示例
    out = new DataOutputStream(new BufferedOutputStream(
                  new FileOutputStream(dataFile)));
    ```

注： 使用同一个Stream 多次接收相同的 object 实例会得到相同的引用， 同一个object实例使用不同的Stream传输会得到不同的实例引用

### Object Stream

- 接口 [`ObjectInput`](https://docs.oracle.com/javase/8/docs/api/java/io/ObjectInput.html) and [`ObjectOutput`](https://docs.oracle.com/javase/8/docs/api/java/io/ObjectOutput.html) 这两个接口分别为 [`DataInput`](https://docs.oracle.com/javase/8/docs/api/java/io/DataInput.html) and [`DataOutput`](https://docs.oracle.com/javase/8/docs/api/java/io/DataOutput.html) 接口的子接口
  - 实现类[`ObjectInputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/ObjectInputStream.html) and [`ObjectOutputStream`](https://docs.oracle.com/javase/8/docs/api/java/io/ObjectOutputStream.html). 支持 实现了 [`Serializable`](https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) 接口的 object

### IO 流分类

**第一种：按I/O类型来总体分类**

   **1. Memory** 1）从/向内存数组读写数据: CharArrayReader、 CharArrayWriter、ByteArrayInputStream、ByteArrayOutputStream
            2）从/向内存字符串读写数据 StringReader、StringWriter、StringBufferInputStream
   **2. Pipe管道**  实现管道的输入和输出（进程间通信）: PipedReader、PipedWriter、PipedInputStream、PipedOutputStream
   **3. File 文件流**。对文件进行读、写操作 ：FileReader、FileWriter、FileInputStream、FileOutputStream
   **4. ObjectSerialization 对象输入、输出** ：ObjectInputStream、ObjectOutputStream
   **5. DataConversion数据流** 按基本数据类型读、写（处理的数据是Java的基本类型（如布尔型，字节，整数和浮点数））：DataInputStream、DataOutputStream
   **6. Printing** 包含方便的打印方法 ：PrintWriter、PrintStream
   **7. Buffering缓冲** 在读入或写出时，对数据进行缓存，以减少I/O的次数：BufferedReader、BufferedWriter、BufferedInputStream、BufferedOutputStream
   **8. Filtering** **滤流**，在数据进行读或写时进行过滤：FilterReader、FilterWriter、FilterInputStream、FilterOutputStream过
   **9. Concatenation合并输入** 把多个输入流连接成一个输入流 ：SequenceInputStream 
  **10. Counting计数** 在读入数据时对行记数 ：LineNumberReader、LineNumberInputStream
  **11. Peeking Ahead** 通过缓存机制，进行预读 ：PushbackReader、PushbackInputStream
  **12. Converting between Bytes and Characters** 按照一定的编码/解码标准将字节流转换为字符流，或进行反向转换（Stream到Reader,Writer的转换类）：InputStreamReader、OutputStreamWriter

**第二种：按数据来源（去向）分类**
  **1、File（文件）：** FileInputStream, FileOutputStream, FileReader, FileWriter 
  **2、byte[]：**ByteArrayInputStream, ByteArrayOutputStream 
  **3、Char[]:** CharArrayReader, CharArrayWriter 
  **4、String:** StringBufferInputStream, StringReader, StringWriter 
  **5、网络数据流：**InputStream, OutputStream, Reader, Writer 
  **6、字节流** InputStream/OutputStream

### IO 包 结构图

![img](.\pic\IO包结构.png)

### 命令行 IO 

使用 [`Console`](https://docs.oracle.com/javase/8/docs/api/java/io/Console.html) 类 System.console()

## NIO

### NIO 包结构

![nio包结构](pic/nio包结构.png)

| 包名称                  | 使用/目的                                                                    |
| :---------------------- | :--------------------------------------------------------------------------- |
| java.nio                | 它是 NIO 系统的顶级包，NIO 系统封装了各种类型的缓冲区。                      |
| java.nio.charset        | 它封装了字符集，并且还支持分别将字符转换为字节和字节到编码器和解码器的操作。 |
| java.nio.charset.spi    | 它支持字符集服务提供者                                                       |
| java.nio.channels       | 它支持通道，这些通道本质上是打开 I/O 连接。                                  |
| java.nio.channels.spi   | 它支持频道的服务提供者                                                       |
| java.nio.file           | 它提供对文件的支持                                                           |
| java.nio.file.spi       | 它支持文件系统的服务提供者                                                   |
| java.nio.file.attribute | 它提供对文件属性的支持                                                       |

----
### IO 包和 NIO 包的区别

|        IO        |             NIO              |
| :--------------: | :--------------------------: |
|  阻塞 I/O 操作   |       非阻塞 I/O 操作        |
| 面向流(Stream)的 | 面向缓存(Channel & Buffer)的 |
|    通道不可用    |  通道可用于非阻塞 I/O 操作   |
|   选择器不可用   | 选择器可用于非阻塞 I/O 操作  |

----

### [`Path`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Path.html) NIO 包的入口

用来表示路径，即，文件夹 或 文件，通常可以和 Files 工具类一起使用。

#### 创建 Path

使用 Paths 工具类

```java
Path p1 = Paths.get("/tmp/foo");
Path p3 = Paths.get(URI.create("file:///Users/joe/FileTest.java"));
Path p5 = Paths.get(System.getProperty("user.home"),"logs", "foo.log");
```



注 [`Files`](https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html) 是 nio包下的工具类， 注意于原 io 包下的 [`File`](https://docs.oracle.com/javase/8/docs/api/java/io/File.html) 类区分。

Files 提供文件相关的 创建、删除、移动、读、写、匹配、类型检测等常规操作

### Files 工具类 NIO 包的主要入口之一, 表示文件

注意: 

- 使用完毕后必须 close() 以释放资源
- 使用时捕获对应异常



### 文件读写:

对于小文件, 一次性读写

```java
Path file = ...;
byte[] fileArray;
fileArray = Files.readAllBytes(file);

Path file = ...;
byte[] buf = ...;
Files.write(file, buf);	
```

对于普通文件, 使用 Stream I/O

```java
Path file = ...;
try (InputStream in = Files.newInputStream(file);
    BufferedReader reader =
      new BufferedReader(new InputStreamReader(in))) {
    String line = null;
    while ((line = reader.readLine()) != null) {
        System.out.println(line);
    }
} catch (IOException x) {
    System.err.println(x);
}

public class LogFileTest {

  public static void main(String[] args) {

    // Convert the string to a
    // byte array.
    String s = "Hello World! ";
    byte data[] = s.getBytes();
    Path p = Paths.get("./logfile.txt");

    try (OutputStream out = new BufferedOutputStream(
      Files.newOutputStream(p, CREATE, APPEND))) {
      out.write(data, 0, data.length);
    } catch (IOException x) {
      System.err.println(x);
    }
  }
}
```

对于更大文件和流, 使用 buffered Stream I/O

```java
Charset charset = Charset.forName("US-ASCII");
String s = ...;
try (BufferedWriter writer = Files.newBufferedWriter(file, charset)) {
    writer.write(s, 0, s.length());
} catch (IOException x) {
    System.err.format("IOException: %s%n", x);
}
```

使用 Channel 读写

```java
public static void readFile(Path path) throws IOException {

    // Files.newByteChannel() defaults to StandardOpenOption.READ
    try (SeekableByteChannel sbc = Files.newByteChannel(path)) {
        final int BUFFER_CAPACITY = 10;
        ByteBuffer buf = ByteBuffer.allocate(BUFFER_CAPACITY);

        // Read the bytes with the proper encoding for this platform. If
        // you skip this step, you might see foreign or illegible
        // characters.
        String encoding = System.getProperty("file.encoding");
        while (sbc.read(buf) > 0) {
            buf.flip();
            System.out.print(Charset.forName(encoding).decode(buf));
            buf.clear();
        }
    }    
}
```



### Channel

![chaneel-buffer-java program](pic/Channel-buffer.png)

channel 可以双向作为 文件/设备/网络 和 buffer 之间的传输媒介，通常与操作系统文件描述符具有一对一关系，用于提供平台独立操作功能。

Channel 接口方法：
close();
isOpen();

实现类常用方法：
write();
read();

----
### Buffer
Buffer 的属性：
capacity: 容量
limit: 游标
position: 当前位置
mark: 复位位置标记

```java
//methods:
allocate(int capacity);         // 分配J在VM内存， 数据量大师性能更好， 数据量小时性能略低可以忽略
allocateDirect(int capacity);   // 分配在系统内存， 因为分配系统级内存比分配JVM内存更耗时间

capacity();
limit();
limit(int newLimit);
position();
mark();

clear(); // limit = capacity, position = 0;
filp(); // limmit = position, position = 0;
rewind();// unchange limit, position = 0; for re-read/write
reset();// position = mark;

get();
put();
order(ByteOrder bo); //设置大端小端模式
```

----
### Channel 实现类

FileChannel:

```java
String relativelyPath = System.getProperty("user.dir");
FileInputStream input = new FileInputStream(relativelyPath + "/testin.txt");
ReadableByteChannel src = input.getChannel();
FileOutputStream output = new FileOutputStream(relativelyPath + "/testout.txt");
WritableByteChannel dest = output.getChannel();

ByteBuffer buffer = ByteBuffer.allocateDirect(20 * 1024);// 20 MB
while (src.read(buffer) != -1) {
    // The buffer is used to drained
    buffer.flip();
    // keep sure that buffer was fully drained
    while (buffer.hasRemaining()) {
        dest.write(buffer);
    }
    buffer.clear(); // Now the buffer is empty, ready for the filling
}

//methods:
read();
wirte();
size();
transforFrom(ReadableChannel src, long position, long count);
transforTo(long position, long count, WritableChannel target);
position();
truncate(long size);
open();
map();
lock();
tryLock();
```

DatagramChannel：//for UDP

```java
DatagramChannel ch = DatagramChannel.open();
DatagramChannel ch = DatagramChannel.close();
//methods:
bind(SocketAddress local);
connect(SocketAddress remote);
disconnect();
isConnected();
getLocalAddress();
getRemoteAddress();
socket();
validOps();

open();
read(ByteBuffer dst);
write(ByteBuffer src);
receive(ByteBuffer dst);
send(ByteBuffer src);
setOption(SocketOption<T> name, T value);
```

SocketChannel: // for TCP

```java
SocketChannel ch = SocketChannel.open();
ch.connect(new InetSocketAddress("somehost", someport));

SocketChannel ch = SocketChannel.close();
ch.connect(new InetSocketAddress("somehost", someport));

//methods:
bind(SocketAddress local);
connect(SocketAddress remote);
finishConnect();
getLocalAddress();
getRemoteAddress();
isConnected();
shutDownInput();
shutDownOutput();
socket();
validOps();

open(SocketAddress remote);
read(ByteBuffer dst);
write(ByteBuffer src);
setOption(SocketOption<T> name, T value);
```


----
### Selector

用于单线程中处理多个 channel,即多路复用器.通过减少线程，继而减少线程切换所带来的开销，提高性能。

selector通过 open()/openSelector() 方法开启，SelectableChannel(要求非阻塞) 通过 register()方法注册到Selector，并指定自己的 intsert set (int 型 可通过位运算得到不同操作的并集)
Selector通过select()/selecNow()查询可用的通道数量，之后可用selectKeys()得到可用的SelectionKey对象。
返回可用通道Set.


SelectKey 方法():
```java
key.attachment(); //返回SelectionKey的attachment，attachment可以在注册channel的时候指定。通过key.attach(theObject)将Object绑定在Key上;
key.channel(); // 返回该SelectionKey对应的channel。
key.selector(); // 返回该SelectionKey对应的Selector。
key.interestOps(); //返回代表需要Selector监控的IO操作的bit mask
key.readyOps(); // 返回一个bit mask，代表在相应channel上可以进行的IO操作。

// 查询通道是否对 某操作 就绪
boolean isAcceptable();//是否可读，是返回 true
boolean isWritable()：//是否可写，是返回 true
boolean isConnectable()：//是否可连接，是返回 true
boolean isAcceptable()：//是否可接收，是返回 true
```

Selector维护的三个集合
- key set          : 由keys()方法返回，表示注册到Selector的通道；
- selected key set : 由selectedKeys()返回,表示当前可用通道集合；
- cancelled key set: 不可用但是还未在Selector中注销的通道

select操作 返回的是自上次调用select()方法 到 本时刻进入就绪状态的通道数量

**注： Selector是线程安全的，但是SelectionKey不是**

selector 代码示例：

```java
ServerSocketChannel ssc = ServerSocketChannel.open();
ssc.socket().bind(new InetSocketAddress("localhost", 8080));
ssc.configureBlocking(false);

Selector selector = Selector.open(); // 开启selector
ssc.register(selector, SelectionKey.OP_ACCEPT); // 注册

while(true) {
    int readyNum = selector.select();
    if (readyNum == 0) {
        continue;
    }

    Set<SelectionKey> selectedKeys = selector.selectedKeys(); // 获取SelectionKeys
    Iterator<SelectionKey> it = selectedKeys.iterator(); // 获取遍历器

    while(it.hasNext()) {
        SelectionKey key = it.next();
        // 根据不同就绪状态做不同操作
        if(key.isAcceptable()) {
            // 接受连接
        } else if (key.isReadable()) {
            // 通道可读
        } else if (key.isWritable()) {
            // 通道可写
        }

        it.remove();
    }
}
```

----

### Pipe

连接两个通道(可在不同线程里)
数据从source 读取， 写入 sink

```java
static Pipe	open() //Opens a pipe.
abstract Pipe.SinkChannel	sink() // Returns this pipe's sink channel.
abstract Pipe.SourceChannel	source() //Returns this pipe's source channel.
```

----

### NIO 编码/解码

- CharsetEncoder 将unicode 编码为 字节流序列 放入 ByteBuffer
- CharsetDecoder 将字节序列解码后传入 CharBuffer

```java
Charset cs = Charset.forName("UTF-8");
CharsetDecoder csdecoder = cs.newDecoder();
CharsetEncoder csencoder = cs.newEncoder();
String st = "Example of Encode and Decode in Java NIO.";
ByteBuffer bb = ByteBuffer.wrap(st.getBytes());
CharBuffer cb = csdecoder.decode(bb);
ByteBuffer newbb = csencoder.encode(cb);
```

----
### FileLock

由 FileChannel 或 AsynchronousFileChannel 的 lock() 或 tryLock() 返回
```java
FileLock.lock(long position, long size, boolean shared);
FileLock.tryLock(long position, long size, boolean shared); // return null for fail
```

