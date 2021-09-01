## linux 学习
[toc]

### 帮助命令 man

#### 如何设置中文man
查找 man 中文包

```yum list |grep man.*zh```

安装man中文包

```sudo yum install man-pages-zh-CN.noarch```

添加 cman 命令别名

```vim .bashrc```

在文件中添加：

>alias cman='man -M /usr//share/man/zh_CN'

重新加载bash,使 cman 生效

```source .bashrc```

另外几种帮助命令
> whatis //命令的简短说明

> info // 命令的说明

#### man 命令分类
(1)、用户可以操作的命令或者是可执行文件 （bash命令包含在1类中）
(2)、系统核心可调用的函数与工具等
(3)、一些常用的函数与数据库
(4)、设备文件的说明
(5)、设置文件或者某些文件的格式
(6)、游戏
(7)、惯例与协议等。例如Linux标准文件系统、网络协议、ASCⅡ，码等说明内容
(8)、系统管理员可用的管理条令
(9)、与内核有关的文件

#### 查看命令的路径
>which //列出程序所在路径

>whereis //搜索程序路径， 会列出所有版本的命令所在路径

### 文件管理

#### 常用命令

- 创建目录
> mkdir
- 创建文件
> touch
- 删除
> rm // -r 递归 -i 提示+确认 -f 不提示
- 移动
> mv src dest // -b 备份 -i 覆盖提示 -f 覆盖不提示 -v verbose
- 复制
> cp src dest //
- 目录切换
> cd
- 列出文件/目录
> ls // -C 纵向输出 -a 列出隐藏目录 --block-size=SIZE -l long -h human readable size -s size -S sort by size

- 查找文件 
> find [path] PATTERN [-exec COMMAND] [-printf fomat]

- 文件查看
> cat
> head -n
> tail -n number_of_line -f // 动态实时
> more
> less

- 文件内容匹配
> grep -e PATTERN

### 管道和重定向

#### 管道
批处理命令连接执行，使用 |
串联: 使用分号 ;
前面成功，则执行后面一条，否则，不执行:&&
前面失败，则后一条执行: ||


###
\>
:>
\>\>

### 环境变量
启动帐号后自动执行的是 文件为 .profile，然后通过这个文件可设置自己的环境变量；
修改PATH示例
> PATH=\$APPDIR:/opt/app/soft/bin:\$PATH:/usr/local/bin:\$TUXDIR/bin:\$ORACLE_HOME/bin;export PATH


## 文本处理

### find命令

