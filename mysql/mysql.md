mysql.md
## Win 下的安装
1. 解压
2. 配置环境变量
3. 配置my.ini
4. mysqld --initialize-insecure 初始化mysql(无初始密码) mysqld --initialize --console (屏显初始密码)
5. mysqld --install 注册服务
6. net start mysql 启动服务 

## 常用命令

### 登陆
mysql -uroot -p

### 修改密码
mysql> alter user 'root'@'localhost' identified by '123456';
mysql> flush privileges;

### 备份
mysqldump -u[uname] -p[pass] [dbname] > [backupfile].sql

### 导入
mysql -u[uname] -p[pass] [dbname] < [backupfile].sql

TIMESTAMP column with CURRENT_TIMESTAMP 问题： --> 升级到5.5以上



## MySQ逻辑架构

连接层 jdbc odbc 等等连接(池)技术 

服务层 api, sql解析, sql优化等

引擎层 不同的数据库引擎,插件式

存储层 底层文件存储

## MyISAM 和 InnoDB 区别

|          | MyISAM                           | InnoDB                           |
| -------- | -------------------------------- | -------------------------------- |
| 外键     | 不支持                           | 支持                             |
| 事务     | 不支持                           | 支持                             |
| 行锁     | 不支持                           | 支持                             |
| 缓存     | 只缓存索引不缓存数据             | 索引和数据都缓存, 对内存要求较高 |
| 表空间   | 小                               | 大                               |
| 关注点   | 性能 偏读写                      | 事务                             |
| 索引类型 | 非聚族索引(叶子节点存数据的指针) | 聚族索引(数据放在叶子节点块)     |

注: InnoDB 的行锁是基于索引实现的, 锁分为三种 行锁: Record Lock,  间隙锁: Gap Lock ，锁定一个范围，但不包含本身.   Next-Key Lock : 锁定一个范围,并且锁定记录本身

## 聚族索引的优缺点

优点:

- 数据和索引一起加载到内存, 大多数情况下可以减少磁盘IO
- 移动数据行时只维护主索引, 不用再维护辅助索引
- 适合排序, 因为数据的物理存放顺序和索引是一致的

缺点:

- 辅助索引(二级索引)需要去主索引在查找一次来获取数据

==注: 因为InnoDB 聚族索引的使用, 所以主索引(主键) 最好为自增, 使得数据插入不会对索引树产生较多的分裂和移动操作.==

==比较好的选择是 雪花算法, 既保证全局唯一,又保证单调性==

## ==数据库隔离级别==

- ==读未提交==
- ==读已提交==: (快照读) ==Oracle, Sql Server==
  - 可以防止脏读
- ==可重复读==: ==Mysql== MVCC（多版本并发控制）一个事务开始，会得到相应的版本号，在这个事务里面读同一个版本号的同一个数据，都是相同的，解决不可重复读。
  - 防止脏读和不可重复读(事务中别的事务提交了update,导致两次读取的数据不一致)
- ==串行化==
  - 防止脏读,不可重复读,幻读(两次读取之间,别的事务进行了 insert delete 导致两次读取集合的数量不同)
  - 效率极低, 完全不支持并发

## SQL 执行顺序

FROM

ON

​	JOIN

WHERE

GROUP BY

HAVING

SELECT

DISTINCT

ORDER BY

LIMIT



## 七(五)种 JOIN

- LEFT JOIN
  - ON A.Key = B.key   ====> 全A
  - ON B.key = NULL   ====> A但是不包含与B的交集, 即 A-B
- RIGHT JOIN
  - ON A.key = B.key	 ====> 全B
  - ON A.key = NULL     ====> B但是不包含与A的交集, 即, B-A
- INNER JOIN     ====> AB 交集



- FULL OUTER JOIN  (MySQL没有)
  - ON A.key = B.key  ====> 全集
  - ON A.key = NULL OR B.key = NULL   ====> 全集不包含AB交集



## 索引

优势: 排序已经做好, 提高==查找==和==排序==的效率

缺点: 增删改需要维护索引, 效率降低

需要索引的字段: 

- 常用的查询字段和排序字段适合建立索引(多字段联合的可以做复合索引) 

- 用于group by 的字段适合建立索引

- 除了必要的场景, 一张表最好不建多余的索引, 

不适合建立索引的字段: 

- 经常改动的字段, 

- 不用于查询条件的字段
- 重复内容过多的字段

### 索引分类

- 单值索引: 建立在单字段上的索引
- 复合索引: 多字段联合索引
- 唯一索引: 索引必须唯一, 但是允许空



## ==慢sql排查:==

- 执行时间长
  - sql 烂
  - 未击中索引
    - 索引失效
  - 关联join多
  - 服务器调优, (缓冲,线程池,硬件)
- 等待时间长 --> 其他问题
  - 传输数据多,网络慢
  - 磁盘占用高
  - ...

## Explain 详解

- ==id:==
  - 表示 表的加载顺序, id 越大越先加载 相同 id 从上到下执行

- select_type:
  - SIMPLE: 简单查询, 不包含子查询, 不包含 UNION
  - PRIMARY : where子句 有子查询 或者 UNION 时, 最外层的 SELECT
  - SUBQUERY 子查询(SELECT 或者 WHERE　语句中)
  - DERIVED 衍生表(临时表)
  - UNION -- 联合查询　被 UNION 的表, 若UNION 在FROM子句的子查询中, 则外层被标记为 DERIVED 
  - UNION RESULT: 从 UNION 表获取结果的 SELECT
- table:
  - 加载的表
- ==type:== 访问类型 从上到下性能越来越差, 一般至少 range, 最好在 ref 以上
  - system : 单表单行
  - const: 唯一索引 = 固定值
  - eq_ref: 查询和比对的所有字段 都是 唯一索引
  - ref: 非唯一索引 = 某值, 查询出特定的一组行, 然后扫描
  - fulltext: 全文检索, 需要 FULLTEXT 类型索引支持 用于 REGEXP MATCH() against() 模糊匹配
  - range: 索引在一定范围内查找, 一般 >, <, between, in 这种 
  - index: 全索引扫描, 因为索引文件比数据文件小得多, 所以比全表扫描好
  - ALL: 全表扫描
- possible_keys
  - 可能可以用到的索引
- ==key==
  - 实际用到的索引 (注: 无where子句,但是select 的字段 刚好符合索引时, 会用到覆盖索引, 会出现possible_keys 为 null, 但是  key 有用到索引, 相当于不需要回表扫描)
- key_len
  - 索引使用的字节数, 这里显示的是最大可能长度, 并非实际使用长度, 即, 根据表定义来计算出的长度
- ref
  - 显示用到了那些字段被用于和索引进行对比, 一般为 字段名, const, func, null
- ==rows==
  - 需要查找行数的一个预估值
- ==Extra== 其余可能很重要的信息 
  - 例如 Using index, 表示只用索引就完成了查询, 没有回表, 一般意味着性能很好
  - Using where, 表示使用了 WHERE 做过滤
  - Using temporary, 表示需要创建临时表, 常见于 order by, group by,  需要建立索引并优化
  - Using filesort, 表示使用了外部排序, 一般是由于 排序的字段 没有索引, 需要建立索引并优化
  - Using Join Buffer: 表示 join 比较多, 可以调大缓冲区的 join buffer
  - impossible where: where 匹配不到任何东西
  - distinct: 表示找到了匹配行后,就不再搜索了, 就是用了 DISTINCT 关键字



## ==索引什么时候会失效==

- 最左匹配原则, 联合索引 如果从左开始缺少某些 索引列, 则无法使用这个联合索引
- 联合索引 使用范围查询时, 范围查询条件右侧的 索引列失效
- like 查询如果用了 左 通配符 %, 索引会失效
- 在索引列上进行计算操作,函数操作, 无论是 + * 截取 等, 会导致索引失效
- 对索引列使用 !=,  <>,  is null, is not null 这些匹配方式, 会导致索引失效
- 触发了隐式的格式转换时, 会导致索引失效, 例如 字符串 列 匹配时 不加单引号, 转为了 数字型匹配
- 使用 OR 条件时, 如果两个条件 不是全都加了索引, 那么索引会失效.
- 使用 非主键索引时 如果 mysql 优化器 预估 所需访问的行锁 大于一个 阈值 (优化器会综合考虑 表大小, 行数量, IO块大小, 一般来说这个阈值在10%左右) 会放弃使用索引, 改用全表扫描. 原因是非主键索引查找到匹配行后,需要取出主键值,然后再走一遍主索引.优化器会认为此时效率不如直接全表扫描 这也是为什么不建议在区分度的字段使用索引的原因. 这在 时间做条件 或者区分度低的索引字段上会出现, 这里会推荐使用 limit 限制一次访问的大小
- 



## 建立索引和使用索引的一些经验

复合索引 range 查询之后的 字段 索引失效

- 复合索引 在 range 类型 字段引用之后的字段, 索引会失效, 所以最好把会涉及 range 查询的字段 剔除出索引 以保证符合索引中的字段不会因为 range 查询失效, 或者把 range 查询的字段 尽量往复合索引的后面放.

对于连接查询:

- 使用小表驱动大表, 并给大表加索引, 作为驱动的表无论如何都是要扫描的, 加不加索引 区别在于查询类型是 index 还是 null

对于嵌套查询:

- 优先优化内层查询

对于 join:

- 如果无法增加索引, 在内存充足的情况下, 可以尝试调大 Join Buffer
- 使用 EXIST, NOT EXIST 代替 in,  not in



## 模糊查询

- like
  - INSTR()
  - POSITION()
  - LOCATE()
- REGEXP 正则
- where MATCH(col_name) against ("value")  在设置了 FULLTEXT 全文检索索引的列上可以用

## MySQL 主从复制

主机使用 binlog 记录所有数据的更改, 从机拉取 binlog 并放入到 自己的 relay log 并且重做操作

## Mysql数据库日志

- redo
- undo

## 关于跨库跨表查询

一般在