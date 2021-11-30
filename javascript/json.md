# JSON

## javaScript json 转换

```javascript
var jsonObj = JSON.parse(jsonStr);
var jsonStr = JSON.stringify(jsonObj);
```

## Jackson
导入

```xml
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.13.0</version>
</dependency>
```

使用
```java
ObjectMapper mapper = new ObjectMapper();
String str = mapper.writeValueAsString(obj);
```

## FastJson
导入

```xml
<dependency>
    <groupId>com.alibaba</groupId>
    <artifactId>fastjson</artifactId>
    <version>1.2.76</version>
</dependency>
```

```java
    String str = JSON.toJSONString(list);
    // 其他方法
//  JSON.toJSONStringWithDateFormat();
//  JSON.parse(str);
//  JSON.parseObject(str,obj.class);
//  JSON.parseArray(str,obj.class);
//  JSON.toJavaObject(jsonObj,obj.class);
```

## 其他工具

GSON