# Ajax

**作用:**

==异步数据请求,不用刷新全部页面==

```javascript
$.ajax({
    url: "${pageContext.request.contextPath}/ajax/getbook",
    type:"post",
    dataType:"json",
    success: function (data){
    console.log("data = "+ data);
	}
});
```

