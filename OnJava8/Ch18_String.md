### 正则表达式
Pattern.compile("regx") 来编译正则表达式
pattern.matcher("target") 来进行匹配，会返回 Matcher 对象

Matcher 对象用：
matches(), lookingAt(), find(), find(int start) 来判断是否有匹配
用
group(), group(int index), start(int groupId), end(int groupId), groupCount() 来操作组

Pattern.compile(String regex, int flag) flag 来控制一些模式，比较重要的有 
(?m) 多行模式
(?u) unicode模式
(?i) 大小写不敏感模式
(?x) comment模式 空格符将被忽略，以 # 开始直到行末的注释也会被忽略。

