
## 命令

### 配置相关
查看配置
- git config --list --show-origin

### 帮助
- git help
- git [command] -h
  
### 项目相关

拉取
- git clone \[url\]

初始化
- git init

查看状态
- git status

添加文件
- git add

忽略文件
使用.gitignore文件标注

对比文件
- git diff 对比尚未暂存的文件
- git diff --staged 对比上次提交的版本
- git difftoo 使用插件/图形化工具查看diff

提交更新
- git commit -m "commit message"
- git commit -a -m "msg" //简化省略 git add 步骤

移除
- git rm [file]
- -f //强制删除
- --cached 删除仓库中的文件，但是本地保留

移动文件/修改名称
- git mv


撤销 ！！慎重撤销
- git commit --amend //将暂存区数据提交，和上一次提交合并，并能更改提交信息

### 审阅相关

查看历史
- git log //查看提交历史
- git log -p [-数量] //查看详细修改历史，适用于代码审查
- git log --stat //查看修改总结
- git log --graph //图形展示