# Go

## 反射

[reflect package](https://pkg.go.dev/reflect)
[reflection 说明 - Rob Pike](https://go.dev/blog/laws-of-reflection)

reflect 包中两个重要的类型

- Type
- Value

**从 interface 到 reflect 类型:**
使用 reflect.ValueOf() 和 reflect.TypeOf() 取得
reflect.TypeOf() 返回的是 interface 的具体类型
reflect.ValueOf() 返回 interface 的值
Value.Kind() 返回的是value的所属类型

**从 reflect 类型 到 interface:**

使用 Value.Interface()方法,将 Value 类型变量转换为 interface{} 类型

**对 Value 类型 赋值:**
只有被 set的 Value 类型变量才可以使用 Value.SetXXX()方法
检测一个 Values 是否有 settability, 使用 Value.CanSet() 方法测试

一个 Value 是否具有 settability, 是要看它是否能影响使用ValueOf()方法得出它的,原来的变量

一般如果需要使用 Value 做更新值的操作,需要将指针传进里 v := reflect.ValueOf(&x)
得到一个 Pointer 类型的 Value,然后去 s:=v.Elem()之后调用 s.SetXXX()赋值 //try5
若传入的 x 为一个struct,则使用 Field(int).SetXXX() 取值赋值 // try6 另注:Field 只能最导出的字段进行操作,未导出的字段不具备 settability
