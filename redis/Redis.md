# Redis palyground

> 性能测定: 读 11W每秒, 写 8W 每秒
>
> NoSql特性:
>
> ​	数据之间没有关系,方便横向拓展(集群)
>
> ​	数据类型多样,对于数据库设计要求低
>
> NoSQL种类
>
> ​	KV: Redis
>
> ​	文档型(BSON): MongoDB(C++) ConthDB
>
> 列存储数据库:
>
> ​	HBase
>
> 图关系数据库:
>
> ​	Neo4j 	InfoGrid

Redis 基本上实际的业务场景就是缓存, 自己的实际项目中, 我们用 redis 做了登录用户的服务端缓存,登陆后把用户id 和认证token,设备码 缓存在 redis 里, 做了一个免登录

### Redis 工作线程是单线程, 但Redis 整体不是单线程的

Redis不是单线程, 只是核心的工作线程是单线程模型(工作线程包括网络,IO,计算,过期等等,6.0以后有IO多线程,只是数据搬运可以多线程,每条线程负责对应一个客户端, 多线程IO主要是更高的利多核,提高网卡-内核吞吐量), 

Redis工作线程设计为单线程, 是因为Redis是基于内存的, 多线程切换上下文反而降低效率(因为除了持久化之外,工作进程中几乎没有和硬盘读写,所以单线程效率极高,单线程也可以IO多路复用).

用多线程是因为 cup 需要等待 IO 网络 等缓慢的阻塞操作, 为了不让 cpu 性能浪费

而Redis主要操作内存, cpu 没那么多空闲时间, Cpu不是性能瓶颈, 瓶颈在于内存,网络带宽等. 单线程完全足够使用, 至于读写磁盘的持久化操作,都是fork() 一个子进程来完成的.

(epoll模型, epoll不负责数据的读写,只负责通知读写的事件)



## 安装

下载 https://redis.io/

放到 opt

tar -zxvf redis-6.2.6.tar.gz 解压

make 编译, 没有gcc环境的需要先安装gcc

安装完之后可以 make test 检测

make install 将 redis 命令安装到 usr/loacl/bin

修改 redis-config (daemonize yes)开启后台运行

src/redis-serve 开启服务



## 系统相关命令

```
dbsize 查看数据库大小
SELECT [0-15] 选择数据库
```



## Key-Value命令

```
#################################
keys * // 查询所有 key

#################################
flushdb // 清空当前数据库
flushall // 清空所有数据库

#################################
# KEY GET SET
#################################
SET key value // 设置 key value
GET key value // 获取 key value

SET key value EX time // 设置 key-value 同时设置过期时间的简写
SETEX key expire value // 设置过期简写,单位秒

SETNX key value // NX 表示 not exists 如果 key 不存在, 才创建 key

MSET key value [key value] // 批量增加值
MGET [key] // 
MSETNX [key value] // 如果 keys 不存在, 批量新增 kv, 此操作为原子的, 一个不成功则全部 kv 都不成功

GETSET key value // 先获取再设置, 这个操作是原子的,这里的原子性指不会再get set 之间有其他的命令插入其中,可以用来设计锁(get 然后 getset, 若两次取得的值相同,说明拿到锁,不同则说明获取锁失败)

#################################
DEL key

#################################
EXISTS key // 查询数据是否存在

#################################
INCR key  // 自增1 ,会有溢出问题 值限制在 [-2^63 , 2^63 -1]
INCRBY key offset // 自增offset

DECR key // 自减
DECRBY key offset

#################################
DECR key
DECRBY key offset

#################################
RENAME key newkey
RENAMEX key newkey // key 不存在时不会创建 key

#################################
TYPE key // 查询 key 的类型

#################################
EXPIRE key time // time unit = secound， -1 means never expire
TTL key  // 查询 key 剩余过期时间， -2 = 已过期
PERSIST key  // 取消 key 的过期时间

#################################

```



## 数据类型

### String(同时也是数字)

```
APPEND key value // 向 key 中追加 字符串, key 不存在就新增一个key
STRLEN key // 字符串长度

GETRANGE key start to // substr [start, to], -1 表示末尾
SETRANGE key offset value // 从 offset+1 位置 开始替换字符串


####################################################
#
# 可以用 json 形式 增加 Object
#
####################################################

set user:1:name user1
set user:1:age 18

// 注: 可以这么设置, 但是key 是 user:1:name user:1:age
// get user / get user:1 是拿不到值的,因为不是存在的 key
// 存 object 可以用 Hash

```

可以用作计数器,缓存,锁等场景

### List(双向链表)
```
RPUSH key value
LPUSH key value
LPUSHX key value // 当 key 不存在时, 不会自动创建队列 key
RPUSHX key value 

LPOP key        // 空队列和 key 不存在 返回 nil
RPOP key

LINDEX key index // 查询 第 index 元素

LREM key count elemnt // remove移除 count 个 element

LSET key index value // 只有 key 和 element 存在才能设置成功, 用于更新已有list中的元素

LINSERT ket [BEFORE|AFTER] pivot value //
向指定元素(pivot)之前或之后插入一个值

LLEN key // 空队列和key不存在都返回 0; 数据结构不是队列 返回 error
LRANGE start end // 0 为起始 返回 list[start, end], 可以用负数 -1 相当于队尾; 越界不返回 error， 只尽可能返回 sublist, 没有对应元素时返回空



LTRIM key start end // 对 key 截取 [start,end] 即, 会改变原来的 key 内容

RPOPLPUSH src dest // 从 src rpop
对 dest lpush 这个操作是原子的
// 注: 没有 lpoprush

```
小技巧: 

lpush lpop 相当于栈

rpush lpop 相当于队列



### Set

```
SADD key value // 向Set添加 元素
SREM key // 移除元素, 返回 1 表示 成功， 0 表示 元素不在 Set 里

SMEMBERS key // 返回 Set 中所有元素

SISMEMBER key value // 查询元素是否在key Set中 返回 1 表示 存在, 0 表示 不存在

SCARD key // 返回set成员数 = size()

SUNION key1 key2 [...] // 并集 返回的结果为临时集合,不会改变原来的set
SINTER key1 key2 [...] // 交集 --> 共同关注,推荐好友

SPOP key n // 随机弹出 n 个元素, 空Set或者Set不存在返回 nil

SRANDMEMBER key n // 随机选出 n 个 元素, 不从 Set 中移除， 空Set或者Set不存在 返回 nil

smove src dest member // 把 member从 src 移动到 dest
```


### Hash(Map集合--> key-Map)

```
HSET key field value // 存贮 键值对 到 key

HSETNX key field value
HMSET key field1 value1 [field2 value2 ] // 单次存储多个键值对 现在已经被废弃,可以直接用HSET
// 存储对象示例 HMSET class:obj1 attribute1 value1, attribute2 value2, attribute3 value3

HDEL key field1 [field2] // 删除键值对

HGET key field // 读取键对应的值
HGETALL key // 读取 key 所有键值对

HLEN key    // length

HEXISTS key field // exists 

HKEYS key // key set
HVALS key // value set

HINCRBY key field offset // 对 field 增加 offset
```



### Sorted Set(ZSet)有序集合

```
ZADD key score value // 添加元素
ZREM key value // 移除

ZSCORE key member // 返回 member 的 score

ZCARD key // = size()
ZCOUNT key min max // 返回分数区间 [min, max] 的所有元素

ZPOPMIN
ZPOPMAX

ZRANGE key start end [WITHSCORES] // 返回 [start, end] 元素, 0 起始, 可用负数, scores 小 --> 大
ZREVRANGE key start end [WITHSCORES] // 类似上边, 但是反序 scores 大 --> 小

ZRANK key member // 返回 元素的排名, 即 下标, 0 起始,
ZCOUNT key min max // 统计区间内的成员个数

ZINCRBY key increment member // 给指定成员 的分数 增加一个增值

ZRANGE 后可以拼接 BYSCORE/BYLEX
ZREMRANAGE 后可以拼接 BYSCORE/BYLEX/BYRANK
```
主要用于排序,权重



### Geo (地理位置数据)

```
GEOADD key longitude latitude member // key 经度(-180,180) 纬度(-85.xx, 85.xx) 名称

GEODIST key member1 member2 [m|km|ft|mi] // 两点之间距离 后边的是可选单位

GEOPOS key member // 获取 member 经纬度

GEOHASH key member// 返回Geohash字符串, 将经纬度转换为字符串,可以从右删除,会逐渐损失精度

GEORADIUS key longitude latitude radius [m|km|ft|mi] [count n]// 以给定经纬度为圆心 获取给定半径内的元素

GEORADIUSBYMEMBER key member radius [m|km|ft|mi] // 以给定元素为圆心, 获取给定半径内的元素
```

底层为 zset



### Hyperloglog

统计基数(不重复的元素的数量) Hyperloglog 可能会有一定误差, 若考虑使用, 需要业务能够容忍一定的误差

使用固定大小的内存 12KB !!!

```
PFADD key values // 添加
PFCOUNT key	// 统计
PFMERGE dest src1 src2 // 合并
```

使用场景: 统计独立用户访问数量 可能会有一定误差



### BitMap

```
SETBIT key offset value // 存储
GETBIT key offset // 获取
BITCOUNT key start end // 统计
BITPOS key bit start end // 返回第一个 0|1 所在的位置

BITOP [AND、OR、NOT、XOR] dest src1 src2 ... // 对位图进行 位操作
```

用于状态筛选和展示, 实际就是以 0|1 组来表示状态, 压缩存储空间



## 事务(实际上就是一个命令队列)

==Redis 单条命令是原子性的,但是**事务内如果发生了错误,其他命令继续执行,没有回滚的概念**==

Redis事务的本质:

一组有序的命令的集合,执行这组命令的时候不允许其他命令执行: 一次性,顺序性,排他性

==Redis 事务没有隔离级别的概念, 只是把一组命令按顺序存起来, 收到执行命令的时候再顺序执行命令组==

Redis 事务的阶段:

开启事务( multi )

命令入队( 普通命令 )

执行事务( exec )

放弃事务( discard ) 只能在没有执行之前放弃事务



事务的Error:

- 编译型异常, 事务中的命令都不会被执行
- **运行时异常, 其他命令会正常执行 ---> 所以说Redis的事务不保证原子性**



## 锁(监视器 watch)

悲观锁: 先加锁,然后再干活

乐观锁: 先干活,提交结果前检查一下是否有别人改动了同一个对象



Redis: 乐观锁 CAS

```
WATCH key
MULTI
XXX
XXX
XXX
XXX
EXEC
```

如果在 watch 执行后, key 被另一个线程修改过, 则事务会放弃, 什么都不发生;

```
# 事务失败后需要先
UNWATCH  // 解锁, 事务exec或discard后,会自动取消监听, 比对version的操作是call exec 之后, run exec 之前

# 获取新的锁 --> 记住新的 odl_value
multi
XXX
XXX
EXEC
```

## Redis 原子性解释

Redis 的指令是原子的, 原因是 单工作线程

Redis 的 pipeline 是原子的

lua 脚本是原子的

事务: 事务内指令之间是顺序的,每条指令都是原子的,但是事务整体不是原子的, 中间有命令出错的话,其他指令会正常执行, redis没有回滚的概念(其实redis 的事务并不是真正的事务,理解为只是把指令先攒到队列里一起顺序执行而已)

另外: 虽然客户端读取的顺序不可控, 但是同一个客户端的 socket 链接里的指令是有序的



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

        // 事务需要保持指令在同一个链接里进行
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

    // 事务保持一个链接
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



## 持久化

### RDB 原理

==主进程 根据配置参数, 每个一段时间 fork() 出一个 子进程, 由于fork()函数的特性, 子进程与组进程共享一部分内存(且有系统内核copy-on-write,写时复制机制支持), 主进程依然负责处理请求, 子进程将内存快照写入临时文件,写入完成后替换 rdb 文件, 完成对 rdb 文件的一次更新.==

==rdb 模式的缺点在于 有可能会丢失上一次更新到程序出错之间的数据.==

==适合大规模数据的恢复, 且对数据完整性要求不是非常敏感的业务.==

#### 参数

```conf
# 多长时间内 至少 n 个key 有过修改, 满足条件则把数据存放到 .rdb.aof 文件
save 900 1
save 300 10
save 60 10000

# 持久化出错后是否继续工作
stop-writes-on-bgsave-error yes

# 是否压缩 rdb 持久化文件
rdbcompression yes # 一般都开,除非业务对cpu负载过高,考虑关掉节省一点点cpu
# rbd 文件校验
rdbchecksum yes
# rdb 文件名
dbfilename dump.rdb
# rdb 文件存储路径
dir ./
```

#### rdb 文件恢复

在 redis 启动目录下若存在 rdb 文件, 则 redis 会自动加载该文件

查看 dir 位置:  启动redis 客户端 `congig get dir`

### 触发机制

1. save 规则
2. flushall
3. 退出 redis

### AOF 相关参数

```
# 追加模式 默认关闭 默认使用 rdb 进行持久化
appendonly no
# aof 文件名
appendfilename "appendonly.aof"

# append 同步模式 单次修改/每秒/关闭同步
# appendfsync always
appendfsync everysec
# appendfsync no

```

#### AOF 原理

子进程记录所有写操作, 根据配置定时 把所有缓存的 写操作 更新到 aof日志文件

优点: 操作日志类型, 可以做回滚, 文件完整性比 rdb 更好一点, 但是还是会丢失一些数据.

缺点: 性能效率不好, 恢复慢, 占用太多存储空间, 所以一般时集群里放在从机做备份, 而主要的持久化机制还是采用 rdb

==注:== 如果只是用 redis 做缓存, 其实完全不必考虑持久化, 毕竟缓存就是断电即失的, 缓存的目的在于提高查找性能, 提高吞吐量, 不在于持久化数据, 持久化的责任交给别的机制

### ==使用建议 :==

有持久化需求的时候, 优先使用 rdb, 对数据丢失的容忍度小时, 再考虑启用 aof, 使用 aof 时 调整 aof 文件的重写大小, 根据服务器性能, 可以调到 GB 级别, 尽量减小 aof 重写 带来的 IO 阻塞和系统波动

## 其他参数

```conf
# 最大客户端连接数
maxclients 10000

# 最大内存设置
maxmemory <bytes>

# volatile-lru -> Evict using approximated LRU, only keys with an expire set.
# allkeys-lru -> Evict any key using approximated LRU.
# volatile-lfu -> Evict using approximated LFU, only keys with an expire set.
# allkeys-lfu -> Evict any key using approximated LFU.
# volatile-random -> Remove a random key having an expire set.
# allkeys-random -> Remove a random key, any key.
# volatile-ttl -> Remove the key with the nearest expire time (minor TTL)
# noeviction -> Don't evict anything, just return an error on write operations.

# 内存不足时的剔除策略
maxmemory-policy noeviction

```



## 发布订阅

更好的方案是使用专业的 MQ( Rabbit MQ, Rocket MQ, Kafka)

```
SUBSCRIBE channel [channel ...] // 订阅, 客户端加入链表字典
PSUBSCRIBE pattern // 模式匹配 订阅

PUBLISH channel message // 发布消息, 遍历订阅链表

UNSUBSCRIBE [channel [channel ...]] // 退订

```



## 主从复制

主机负责写和向从机同步消息, 从机负责读, 一般来说单机20G以上一定要使用集群,提高数据可靠性,降低服务器成本. 但是设置了集群之后, 因为需要减少从机联机时的全量复制, 一般把集群里设置为主机集中的每个主机的内存大小设置小一点,一般2G-8G左右

### 使用目的:

- 数据冗余: 热备份
- 故障恢复
- 负载均衡
- 高可用基础: 哨兵(至少1主3从,才可以实行选举机制),集群的基础

### 查看当前 service 信息

```
info replication //
slaveof ip port // 设置当前 redis 为 ip:port 的从机
slaveof no one // 取消当前从机的主机, 当前redis service 恢复为主机模式
```

### 从机配置文件修改

```
repilicaof ip port // 指定主机
masterauth // 指定主机密码
```



## 哨兵模式

优点: 可用性更高, 健壮性更好

缺点: 不好在线扩容

### 原理(alive监控 + 选举 + 自动 slaveof no one)

启动独立的 哨兵进程 监控所有 redis 节点, 当节点不可用时, 会根据配置的策略选取从机作为新的主机.

哨兵也需要其他哨兵相互监控. 所以哨兵模式最少 6 个线程

3 哨兵, 1 主 2从

当哨兵监控到主机不可用时, 会再本哨兵标记主机不可用, 并通知其他哨兵(成为 主观下线), 当多数哨兵都监控到主机不可用的时候, 会认为主机的确不可用了, 之后会根据策略选取其他节点作为主机



### 哨兵模式配置

```
sentinel monitor <myredis> <ip> <port>	 1
				//名称	ip	port    重选主机的最小哨兵观测数(一个哨兵观测到主机不可用,就可以发起重选主机)

port 26379

# 带密码需要其他配置参数
sentinel auth-pass <myredis> <password>
```

### 使用 redis-sentinel [conf文件] 启动哨兵进程

主机宕机从连后会自动变为从机



## ==缓存穿透 击穿 和 雪崩==

### 缓存穿透: (大量的查不到的数据,直接访问数据库服务器)

当查询的数据不在缓存也不再数据库中的时候, 用户大量多次查询该数据, 造成数据库服务器访问过多以至于宕机.

#### 解决方案一 前置布隆过滤器做缓存的索引

一种数据结构, 对查询参数hash后存入bitmap, 在控制层中进行校验, 如果不符合规则, 则丢弃, 避免底层数据库服务器的查询压力

#### 解决方案二 缓存空对象

空对象也缓存起来,并设置合理的过期时间, 这样以后一定时间内的查询, 在缓存中就知道数据不存在了, 缺点是有可能会缓存混多无用对象,而且有于过期时间,会造成数据有值之后的一小段时间里任然不可用,如果业务对数据及时性,一致性有极高的要求,则可能会造成损失, 优点是实现简单



### 缓存击穿:(热点缓存失效的瞬间引发的高流量)

一个非常热点的key失效的一瞬间, 大量的查询涌入数据库服务区, 造成一瞬间的高流量.

#### 解决方案一 设置热点永不过期(长时间不过期)

#### 解决方案二 加分布式互斥锁

限制对于每个key, 只有一个线程能去查询后端服务, 将高并发的压力转移到分布式锁上(缺点是锁设计的不好会严重影响性能)



### 缓存雪崩(缓存集中失效)

例如对一批缓存进行了集中的过期设置,当这一批缓存失效时,

或者是redis服务器宕机,造成一大批缓存的失效,

就可能引起数据库访问压力突然增大,压力太大数据库服务器可能会宕机.

> 解决:  第三方锁
>
> ​	AKF分治, 每个 key 隔离, 对不同 key 做分区处理

#### redis高可用

使用redis集群,多加redis服务器

#### 限流降级

缓存失效后,根据策略,自动限流降级,通过加锁或者队列控制 数据库查询和写缓存的线程数量

### 数据预热:

预先把热点数据加载进缓存,设置不同的过期时间,使缓存过期尽量均匀平稳可控



## 面试题

### redis 存在线程安全问题吗?

redis 内部因为是 单工作线程的, 所以不会有线程安全问题, 但是业务上的顺序是需要手动维护的

### redis 过期回收, 淘汰

过期回收: 

	- 分批分段删除
	- 请求时在判断是否过期

淘汰:

 - 参数策略 LRU LFU Random TTL 等

### redis 缓存预热

注意过期别集中

### 缓存与数据库的一致性

redis 本来作为缓存,系统设计上应该考虑容忍一定的不一致.

解决方案 cannal 解析 binlog 定期更新缓存



## 附录

> 数据库发展

单服务器时代

​		单台 mysql 一般单表 300 万行, 一定要配备索引

​		读写混合

多数据库服务器时代

​		缓存 Memcached/EhCache + MySQL + 垂直拆分(读写分离)

数据量过大,一个库一张表装不下

​		分库分表,水平拆分 --> MySQL 集群

数据类型增多,关系型数据库不合适

​		NoSQL



MyISAM: 表锁

InnoDB: 行锁(有索引), 无索引还是表锁

