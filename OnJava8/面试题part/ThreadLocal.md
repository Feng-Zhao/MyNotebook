## Thread相关
### ThreadLocal 关系与结构


Thread 对象中包含 ThreadLocalMap, ThreadLocalMap 以 ThreadLoacl 对象为 key value为存储的值


```puml
class Thread{
    ThreadLocalMap threadLocals
}


class ThreadLocal{

}

class ThreadLocalMap{
    Entry[] extends WeakReference<ThreadLocal<?>>
}

class Entry{
    Object value
}
class WeakReference{}


WeakReference <|-- Entry
Entry  *-- ThreadLocalMap
ThreadLocal *-- ThreadLocalMap
ThreadLocalMap *-- Thread
```

### ThreadLocal 的设计目的

ThreadLoacl 是一种线程本地存储机制，使得线程可以独立于其他线程

模型图：
```puml
object Thread
map ThreadLocalMap { 
    key => 
    value =>  value
    }
object ThreadLocal

Thread --> ThreadLocalMap
ThreadLocalMap::key --> ThreadLocal
```

### Thread问题
Thread -> ThreadLocalmap 以及 ThreadLocalMap -> ThreadLocal 之间为**强引用**，在使用**线程池**时，如果不对使用过的ThreadLocal对象进行手动释放，可能会引起**内存泄漏**
释放方法：**手动调用 ThreadLocal 对象实例的 remove()方法**