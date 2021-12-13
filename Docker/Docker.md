# Docker

## Docker 安装 CentOS下

方式一

```shell
# 移除旧版本
sudo yum remove docker \
                  docker-client \
                  docker-client-latest \
                  docker-common \
                  docker-latest \
                  docker-latest-logrotate \
                  docker-logrotate \
                  docker-engine

# 安装yum-utils 为了使用 yum-config-manager
sudo yum install -y yum-utils
# 添加官方镜像仓库
sudo yum-config-manager \
--add-repo \
https://download.docker.com/linux/centos/docker-ce.repo

## (optional) 使用阿里云镜像库
yum-config-manager  --add-repo  http://mirrors.aliyun.com/docker-ce/linux/centos/docker-ce.repo

# 安装 docker engine
sudo yum install docker-ce docker-ce-cli containerd.io

# 启动 docker
sudo systemctl start docker
# 测试 docker
sudo docker run hello-world
```

方式二: 使用官方脚本安装

```shell
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
```



### 卸载 docker

```shell
yum remove docker-ce docker-ce-cli containerd.io

sudo rm -rf /var/lib/docker
sudo rm -rf /var/lib/containerd
```



## docker 基本命令

```shell
docker version
docker info 
docker --help

docker images
docker search <name>
docker pull <name> [version]
docker rm <container-name>
docker rmi <image-name>
docker ps -a
docker logs
docker top <container-id>				查看容器进程信息
docker inspect 							查看容器详细信息
docker exec -it <container> <program>	进入容器 program 一般是 /bin/bash
deocker attach <container>				进入容器正在执行的终端
docker cp 容器id:容器内路径 dest			容器内部文件拷贝到主机

docker run [option] image
# option
--name="name"	容器名字
-d 				后台运行
-it 			交互式运行 进入容器后 exit 退出并停止容器, ctrl+P+Q 退出但不停止容器 
-p				指定容器端口 -p host_port:container_port
-P				大写P,随机给端口
```



## docker 镜像分层

每层都是在上一层的环境上进行操作,便于环境服用.

一般我们的操作都是在容器层进行操作, 打包成新的镜像之后,会把我们修改过的文件作为新的一层压入到镜像中,替换原来的文件.