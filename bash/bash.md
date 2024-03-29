# Bash

## 常用命令

env 查看环境变量
ps 查看进程
man 操作手册
shopt 查看 bash 行为参数

pwd 当前工作目录
echo 打印 -n 参数取消末尾回车符, -e 参数对""包围的字符串做解释

cd 更换当前目录
ls 列出文件

## bash 拓展

|   |   |
|---|---|
|~  |   拓展为用户主目录  ~+ 拓展为当前目录|
|?  |   拓展为任意单个字符|
|\* |   拓展为任意数量字符|
|[] |   拓展指定字符串,例如 [aeiou] [a-z]|
|{} |   为{}内所有字符做拓展 echo 1{a,b,c}2 => 1a2 1b2 1c2|
|{..}   |   为{}内的范围做拓展|
|$  |   将其后的变量做拓展|
|$()|   子命令拓展,拓展为子命令的输出|
|$(())| 算数拓展,拓展为算数表达式的结果|

## 量词语法

|||
|---|---|
|?(pattern-list)：|模式匹配零次或一次。|
|*(pattern-list)：|模式匹配零次或多次。|
|+(pattern-list)：|模式匹配一次或多次。|
|@(pattern-list)：|只匹配一次模式。|
|!(pattern-list)：|匹配给定模式以外的任何内容。|
