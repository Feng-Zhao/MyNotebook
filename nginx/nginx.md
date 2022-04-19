# Nginx

## 安装 使用 docker

`docker pull nginx`

## 运行 并将关键文件映射到系统

`docker run -p 80:80 -p 443:443 --name nginx -v ~/nginx/nginx-conf:/etc/nginx -v  ~/nginx/nginx-html:/usr/share/nginx/html  -v ~/nginx/nginx-log:/var/log/nginx -d nginx`

docker run -p 80:80 -p 443:443 --name nginx \
-v ~/nginx/nginx-conf:/etc/nginx \
-v ~/nginx/nginx-html:/usr/share/nginx/html \
-v ~/nginx/nginx-log:/var/log/nginx \
-v ~/nas:/home/nas \
-v ~/blog:/home/blog \
-v ~/music:/home/music \
-v ~/video:/home/video \
-v ~/zhaofeng_ltd_web:/home/zhaofeng_ltd_web \
-d nginx

## 配置文件

配置文件中分为

- 单指令 以 空格为分隔, 以分号 `;` 为结尾
- 块指令 以 {} 包围
- `#` 为注释

内容结构

```txt
- main
    - events
    - http
        - server
            location
```

### 按文件类型映射不同路径

```nginx
http{
    server{
        listen 8080;
        loaction / {
            root /home/web/image
            # /home/web/image 为路径 root 为标识,不需要变化
        }

        location /proxy {
        # 转发代理
        proxy_pass http://localhost:8080;
        }

        # location 配置多目录,使用 alias
        location /nas {
            alias /home/nas/;
            index  index.html index.htm;
        }
    }
}
```

### 设置监听端口和转发代理

```nginx
http{
    server{
        listen 8080;

        location / {
        # 转发代理
        proxy_pass http://localhost:8080;
        }
        # 未匹配到的 URL mapping 到默认地址
        root /data/up1;
    }
}
```

### 使用正则表达式

```nginx
# 使用 - 标识后边的参数是一个正则表达式
location - \.(gif|jpg|png)$ {
    root /data/images;
}
```
