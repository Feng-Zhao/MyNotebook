## CountDownLatch

### 使用

```java
private static class MyRunnable implements Runnable{
    int id;
    CountDownLatch latch
    MyRunnable(int id,CountDownLatch latch){
        this.id = id;
        this.latch = latch;
    }
    void run(){
        latch.countDown();
    }
}

public void main (Stringp[] args){
    int size = Random.nextInt(10);
    CountDownLatch latch = new CountDownLatch(size);
    for(int id = 0; id < size; id++ ){
        Thread t = new Thread(new MyRunnable(id,latch));
        t.start;
    }
    try{
        latch.await();
        // latch.await(long time, TimeUnit timeUnit);
    }catch(Exception e){
        System.out.println(e);
    }
    System.out.println("latch 归零");
    // do something
}
```

### 源码解析

```java
public class CountDownLatch {
    // 构造函数， 初始化 sync
    public CountDownLatch(int count) {
        if (count < 0) throw new IllegalArgumentException("count < 0");
        this.sync = new Sync(count);
    }

    // 不打断的 await
    public void await() throws InterruptedException {
        sync.acquireSharedInterruptibly(1);
    }
    // 可超时的 await
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireSharedNanos(1, unit.toNanos(timeout));
    }
    // count down
    public void countDown() {
        sync.releaseShared(1);
    }

    // Sync内部类
    private static final class Sync extends AbstractQueuedSynchronizer {

        Sync(int count) {
            setState(count);// 此state 即为 AQS 中的 state
        }

        int getCount() {
            return getState();
        }

        // 尝试获取锁
        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        // 尝试释放锁
        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                // 不断 CAS (c,c-1)直到 c == 0， 既 state == 0;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }
}
```

### CountDownLatch 流程

await()流程：

```puml
start
    :await();
    :acquireSharedInterruptibly(1); 

    note right 
    若中断 则抛出中断异常 
    end note

    if ("tryAcquireShared(arg) < 0 //即 获取锁失败 --> return getState() == 0 ? 1 : -1;")  is (true) then

        note right
            tryAcquireShared() 返回值说明
            负数 表示获取锁失败
            0 表示获取成功，且不需要唤醒后续节点
            正数  表示获取成功，且唤醒后续节点
            ----
            CountDownLatch 当 state == 0 时 返回 1 否则返回 -1
        end note

        group doAcquireSharedInterruptibly(arg)

            :node = addWaiter(Node.SHARED);
            note right
                把线程以共享模式装入队列
            end note

            repeat
                :p = node.prev;
                if (p == head) is (true) then
                    :setHeadAndPropagate;
                    note right
                        更新head(相当于出队),并唤醒后继节点
                    end note
                    end
                else(false)
                    :判断是否需要park,并park();
                endif
            repeat while()

        end group
    else(false)

    endif
end
```
countDown() 流程：

```puml
start
    :countDown;
    group releaseShared
        group tryReleaseShared
            repeat
                : c = getState;
                if (c==0) is (true) then
                    :subReturn false;
                else(false)
                    :CAS(c,c-1);
                    :subReturn c-1 == 0;
                endif
            repeat while()
        end group
        
    
        if(tryReleaseShared return) is (true) then
            repeat
            :reset head;
            :unparkSuccessor;
            note right
                这一步从tail向前找等待唤醒的节点
            end note
            repeat while(head has changed) is(true) not(false)
            :break;
            :return true;
            end
        else(false)
            :return false;
            end
        endif
    end group
        
```