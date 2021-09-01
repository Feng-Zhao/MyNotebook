## 语法

### 基础语法

    $(selector).action()

示例：
- $(this).hide()  隐藏当前元素
- $("p").hide()   隐藏所有 <p\> 元素
- $("p.test").hide()   隐藏所有 class="test" 的 <p\> 元素
- $("#test").hide() - 隐藏 id="test" 的元素

选择器
\# 选择 id
this 选择当前元素
"tag" 选择标签
".class" 选择 class

### 事件函数
```js
$(document).ready(function(){
 
   // 开始写 jQuery 代码...
   // 防止页面未加载完成就执行 js
 
});

// 简洁写法
$(function(){
 
   // 开始写 jQuery 代码...
 
});
```



/RDP/Workflow/