### JavaScript 在网页中的位置
1. <head> 标签中，并且推荐使用外部链接+async的形式
2. 在<body>所有元素加载完之后，</body>标签结束之前，古老的方法，可以保证javascript在DOM加载之后再进行解析，但是对于需要大量js的网站/网页，会有性能损耗；（```async``` 属性诞生的初衷）
3. ```document.addEventListener("DOMContentLoaded", function() { ... });``` ```DOMContentLoaded```事件触发之后


### Javascript如果在被操作的DOM加载之前调用，会引发错误
解决方法，所有Javascript在DOM完全加载之后在进行处理
原生javascript方式：
```javascript
// 在DOM加载完成之后再调用javascript语句
document.addEventListener("DOMContentLoaded", function() {
  . . . 
});
```

```html
// 外部脚本，添加asyn属性，告诉浏览器javascript和html异步加载
<script src="script.js" async></script>
```

### async 与 defer
如果js文件相互有依赖，async不保证js文件的加载完成顺序和出现顺序一致，此时有可能会出现错误，
defer可以保证有js文件的加载完成顺序和出现顺序一致

```html
<script defer src="js/vendor/jquery.js"></script>
<script defer src="js/script2.js"></script>
<script defer src="js/script3.js"></script>
```

### 脚本调用策略小结：
- 如果脚本无需等待页面解析，且无依赖独立运行，那么应使用 async。
- 如果脚本需要等待页面解析，且依赖于其它脚本，调用这些脚本时应使用 defer，将关联的脚本按所需顺序置于 HTML 中。

### Javascript注释
行注释 //
段落注释 /* */


## Javascript语法
### 变量声明

```javascript
let name // 变量声明
const = final_value // 常量声明
var = name // 旧代码

/**
 * var 和 let 的区别在于：
 * 重复声明同名变量 var 不会报错，而let会报错
 * /
/**
 * 变量名规范：
 * 首字母小写驼峰，另外Javascript变量名大小写敏感
 * /
```

### Javascript变量类型
- Number
- String
- Boolean
- Array (a = [1，2，3]形式)
- Object {a:'a',b:'b'}
  
>typeof //查看数据类型