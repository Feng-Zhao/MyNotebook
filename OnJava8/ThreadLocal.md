## ThreadLocal 关系与结构


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

## ThreadLocal 的设计目的

ThreadLoacl 是一种线程本地存储机制，使得线程可以独立于其他线程