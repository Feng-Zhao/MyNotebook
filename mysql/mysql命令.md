
# Mysql 命令

## DB相关

### 授权

`GRANT ALL ON db_name.table_name TO 'your_mysql_name'@'your_client_host';`

### 创建数据库

`CREATE DATABASE menagerie;`

### 数据库查询

`SHOW DATABASES;`

### 查询所有表名

`SHOW TABLES;`

### 创建表

```sql
CREATE TABLE pet (name VARCHAR(20), owner VARCHAR(20),
       species VARCHAR(20), sex CHAR(1), birth DATE, death DATE);
```

### 查看表结构

`DESCRIBE table_name;`

----

## SQL 语句

### INSERT

```sql
INSERT INTO pet
       VALUES ('Puffball','Diane','hamster','f','1999-03-30',NULL);
```

### SELECT

```sql
SELECT what_to_select
FROM which_table
WHERE conditions_to_satisfy;
```

### UPDATE

```sql
UPDATE pet SET birth = '1989-08-31' WHERE name = 'Bowser';
```

### SORT

```sql
ORDER BY row_name DESC -- 降序
ORDER BY row_name ASC -- 升序
```

### 关于 NULL

In MySQL, 0 or NULL means false and anything else means true. The default truth value from a boolean operation is 1

### 模式匹配 两种方式 LIKE/REGEXP_LIKE  

```sql
SELECT * FROM pet WHERE name LIKE 'b%';
SELECT * FROM pet WHERE REGEXP_LIKE(name, '^b'); --  可以用别名 REGEXP() 减短语句长度 
```

### COUNT

```sql
SELECT COUNT(*) FROM pet;
SELECT owner, COUNT(*) FROM pet GROUP BY owner; -- 分组统计
```
