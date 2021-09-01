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