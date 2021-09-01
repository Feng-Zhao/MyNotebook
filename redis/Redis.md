# Redis palyground

## key 相关，命令

### 存取数据

SET key
GET key

### 查询数据是否存在

EXISTS key

### 删除数据

DEL key

### 原子自增（减）

INCR key  // 自增1 ,会有溢出问题 值限制在 [-2^63 , 2^63 -1]
INCRBY key offset // 自增offset

DECR key
DECRBY key offset

### 重命名
RENAME key newkey
RENAMEX key newkey // key 不存在时不会创建 key

### 类型查询
TYPE key

### 设置数据过期时间 

EXPIRE key time // time unit = secound， -1 means never expire
TTL key  // 查询 key 剩余过期时间， -2 = 已过期
PERSIST key  // 取消 key 的过期时间

SET key value EX time // 设置 key-value 同时设置过期时间的简写

## 数据结构

### List

RPUSH key value
LPUSH key value
LPUSHX key value // 当 key 不存在时, 不会自动创建队列 key
RPUSHX key value 

LPOP key        // 空队列和 key 不存在 返回 nil
RPOP key

LLEN key // 空队列和key不存在都返回 0; 数据结构不是队列 返回 error
LRANGE start end // 0 为起始 返回 list[start, end], 可以用负数 -1 相当于队尾; 越界不返回 error， 只尽可能返回 sublist, 没有对应元素时返回空

LINDEX key index // 查询 第 index 元素

### Set

SADD key value // 向Set添加 元素
SREM key // 移除元素, 返回 1 表示 成功， 0 表示 元素不在 Set 里

SCARD key // 返回set成员数 = size()

SISMEMBER key value // 查询元素是否在key Set中 返回 1 表示 存在, 0 表示 不存在

SMEMBERS key // 返回 Set 中所有元素

SUNION key1 key2 [...] // 合并多个 Set

SPOP key n // 随机弹出 n 个元素, 空Set或者Set不存在返回 nil

SRANDMEMBER key n // 随机选出 n 个 元素, 不从 Set 中移除， 空Set或者Set不存在 返回 nil

### Sorted Set

ZADD key score value // 添加元素
ZREM key value // 移除

ZSCORE key member // 返回 member 的 score

ZCARD key // = size()
ZCOUNT key min max // 返回分数区间 [min, max] 的所有元素

ZRANGE key start end [WITHSCORES] // 返回 [start, end] 元素, 0 起始, 可用负数, scores 小 --> 大
ZREVRANGE key start end [WITHSCORES] // 类似上边, 但是反序 scores 大 --> 小

ZRANK key member // 返回 元素的排名, 即 下标, 0 起始,

ZINCRBY key increment member // 给指定成员 的分数 增加一个增值

ZRANGE 后可以拼接 BYSCORE/BYLEX
ZREMRANAGE 后可以拼接 BYSCORE/BYLEX/BYRANK

### Hash

HSET key field value // 存贮 键值对 到 key

HSETNX key field value
HMSET key field1 value1 [field2 value2 ] // 单次存储多个键值对
// 存储对象示例 HMSET obj:obj1 attribute1 value1, attribute2 value2, attribute3 value3

HDEL key field1 [field2] // 删除键值对

HGET key field // 读取键对应的值
HGETALL key // 读取 key 所有键值对

HLEN key    // length

HEXISTS key field // exists 

HKEYS key // key set
HVALS key // value set

HINCRBY key field offset // 对 field 增加 offset
