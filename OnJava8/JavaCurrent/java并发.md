# Synchronize机制

## 对象 对象头

![](/MyNotebook/OnJava8/pic/对象头结构.png)

## Synchronize

Java 6 之前
编译后 为 monitorenter , monitorexit 指令 由操作系统的 mutex lock 指令实现

Jvaa 6 引入

| 锁状态   | mark word | 机制                                                                                                                                                        |
| -------- | --------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------- |
| 无锁     | 001       |                                                                                                                                                             |
| 偏向锁   | 101       | 比对线程 ID， 有竞争时升级为轻量级锁                                                                                                                        |
| 轻量级锁 | 00        | 指向线程私有虚拟机栈中锁记录的指针,结构为 Lock Record{ Mark Word \| Onwer -> Onject } 此时，线程 CAS 自选去尝试获取锁， 等待线程超过 1 个时，升级为重量级锁 |
| 重量级锁 | 10        | 指向重量级锁的指针                                                                                                                                          |
| GC       | 11        |                                                                                                                                                             |
# JUC包

## 无锁编程 CAS

目的：减少用户态/内核态的切换，提高多线程并发效率

由 cpu 指令保证 compare and swap 的原子性： x86: cmpxchg ARM: LL/SC

实现： AtomicXXX 类
> Unsafe类 以及 voloatile long offset

## AQS

内部维护一个 FIFO 双向队列 volatile int state 标识 STATE
队列内结构为 Node: volatile waitStatus 标识 线程状态， volatile thread 表示关联线程， volatile Node prev,next 为前驱/后继节点, Node nextWaiter 表示下一条件等待节点

Node 简化结构

```java
static final class Node {
    /** 超时 取消 */
    static final int CANCELLED =  1;
    /** 后继节点处于等待状态 */
    static final int SIGNAL    = -1;
    /** 节点处于条件等待队列 */
    static final int CONDITION = -2;
    /**
    * waitStatus value to indicate the next acquireShared should
    * unconditionally propagate
    */
    static final int PROPAGATE = -3;

    volatile int waitStatus;
    volatile Node prev;
    volatile Node next;
    volatile Thread thread;
    Node nextWaiter;
}
```

AQS 简化结构

```java
transient volatile Node head;
transient volatile Node tail;
volatile int state;


/**
     * Attempts to acquire in exclusive mode. This method should query
     * if the state of the object permits it to be acquired in the
     * exclusive mode, and if so to acquire it.
     *
     * <p>This method is always invoked by the thread performing
     * acquire.  If this method reports failure, the acquire method
     * may queue the thread, if it is not already queued, until it is
     * signalled by a release from some other thread. This can be used
     * to implement method {@link Lock#tryLock()}.
     *
     * <p>The default
     * implementation throws {@link UnsupportedOperationException}.
     *
     * @param arg the acquire argument. This value is always the one
     *        passed to an acquire method, or is the value saved on entry
     *        to a condition wait.  The value is otherwise uninterpreted
     *        and can represent anything you like.
     * @return {@code true} if successful. Upon success, this object has
     *         been acquired.
     * @throws IllegalMonitorStateException if acquiring would place this
     *         synchronizer in an illegal state. This exception must be
     *         thrown in a consistent fashion for synchronization to work
     *         correctly.
     * @throws UnsupportedOperationException if exclusive mode is not supported
     */
abstract tryAcquire(int arg); // 此方法为模板方法，不同的目的可以有不同的实现，是获取锁的核心方法
void acquire(int arg);
acquireQueued(Node node, int arg);
addWaiter(Node.EXCLUSIVE), arg);
enq(Node node);
```

AQS 流程详解

```puml
    start
        :acquire();
        if (tryAcquire()) then(yes)
            
        else(no)
            partition  "acquireQueued" {
        
                partition  "addWaiter" {
                    note
                        尝试入队成功
                    end note
                    
                    :node.prev = tail;
                    
                    if (CAS (tail,node) // 尝试入队) then(yes) 
                        :(old tail).next = node;
                        
                        :return node;
                    else(no)
                        
                        partition  "enq"{
                            note
                                将节点入队
                            end note 
                            :get tail;
                            repeat :node.prev = tail;
                            repeat while (CAS(tail,node)) is (fail) not (success)
                            :(old tail).next = node;
                                                   
                            :return tail;
                        }
                        :return node;
                    endif
                }
                
                :pred = node.prev;
                if (pred == head) then(yes)
                    note right
                            node != head.next 
                            或
                            没有拿到锁,
                            判断是否park本线程
                        end note
                    if(tryAcquire()) then(yes)
                        note
                            拿到锁,
                            更新head
                        end note
                        :head = node;
                        :(old head).next = null;
                        
                    else(no)
                        
                    endif
                else(no)
                endif
                
                partition "shouldParkAfterFailedAcquire(pred,node)"{
                    note
                        检查前节点状态
                        判断是否需要park本线程
                    end note
                    switch(pred.waitState)
                    case(SIGNAL // -1)
                        :retrun true;
                    case( >0 )
                        while(pred.waitStatus > 0) is (true)
                        note right
                            前节点为取消,
                            将前节点踢出队列
                        end note
                            :node.prev = pred.prev;
                        endwhile(false)
                        :pred.next = node;
                        :return false;
                    case(other)
                        :CAS(pred.ws, SIGNAL//-1);
                        note right
                            前节点为取消,
                            尝试将前节点设置为SIGNAL
                        end note
                        :return false;
                    endswitch
                }

                if(need park) is (yes) then
                    :Thread.currentThread().park();
                else(no)
                endif
            }
        endif
        end      
```

AQS 流程总结
``` puml
start
    :尝试获取锁;
    :尝试入队;
    :入队;
    :检查是否是head.next;
    :尝试获取锁;
    :成功时,更新head;
    :失败时,判断是否需要park;
    :检查前节点状态，更新队列，更新前节点状态，休眠;
    :若需休眠，park() 线程;
    :检查外部中断，设置中断标志，当线程被唤醒时可以去处理中断;
end
```


## ReentrantLock

Lock 接口

```java
void lock();
boolean tryLock();
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
void lockInterruptibly() throws InterruptedException;
void unlock();
Condition newCondition();
```

ReentrantLock

```java
abstract static class Sync extends AbstractQueuedSynchronizer{

    final boolean nonfairTryAcquire(int acquires) {
            // 当前线程
            final Thread current = Thread.currentThread();
            // 获取所状态
            int c = getState();
            // 锁空闲
            if (c == 0) {
                // 尝试CAS
                if (compareAndSetState(0, acquires)) {
                    // 设置当前线程为锁的独占线程
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // 锁 不空闲 
                //  && 当前线程为锁的独占线程
            else if (current == getExclusiveOwnerThread()) {
                // 锁获取次数 计数累加
                int nextc = c + acquires;
                // 超过 int 上线抛出 ERROR
                if (nextc < 0) // overflow
                    throw new Error("Maximum lock count exceeded");
                setState(nextc);
                return true;
            }
            // 锁 不空闲 
                // && 当前线程不是锁的独占线程
            return false;
        }

    // 释放锁
    protected final boolean tryRelease(int releases) {
        // 计算锁状态
        int c = getState() - releases;
        // 如果当前线程未获取锁，则抛出异常
        if (Thread.currentThread() != getExclusiveOwnerThread())
            throw new IllegalMonitorStateException();
        boolean free = false;
        // 锁释放后 空闲
        if (c == 0) {
            // 返回值置为 true
            free = true;
            // 清空锁的独占线程
            setExclusiveOwnerThread(null);
        }
        // 更新锁状态
        setState(c);
        // 返回
        return free;
    }

    /**
     * 非公平模式
     *      直接CAS尝试获取锁，之后进入正常流程
     */
    static final class NonfairSync extends Sync {
        final void lock() {
            // 尝试CAS
            if (compareAndSetState(0, 1))
                setExclusiveOwnerThread(Thread.currentThread());
            else
            // AQS正常获取流程
                acquire(1);
        }
        // AQS tryAcquire() 调用 Sync 的 nonfairTryAcquire(acquires)
        protected final boolean tryAcquire(int acquires) {
            return nonfairTryAcquire(acquires);
        }
    }

    /**
     * 公平模式
     *      获取时直接走正常AQS流程
     *      tryAcquire时 需要 锁空闲 & 无前节点 再尝试CAS
     */
    static final class FairSync extends Sync {

        final void lock() {
            acquire(1);
        }

        /**
         * Fair version of tryAcquire.  Don't grant access unless
         * recursive call or no waiters or is first.
         */
        protected final boolean tryAcquire(int acquires) {
            final Thread current = Thread.currentThread();
            int c = getState();
            // 锁空闲
            if (c == 0) {
                // 无前驱节点时 尝试 CAS
                if (!hasQueuedPredecessors() &&
                    compareAndSetState(0, acquires)) {
                    // CAS成功后设置当前线程为独占县城
                    setExclusiveOwnerThread(current);
                    return true;
                }
            }
            // 如果当前线程是独占线程
            else if (current == getExclusiveOwnerThread()) {
                // 计算锁计数
                int nextc = c + acquires;
                // 溢出抛出 ERROR
                if (nextc < 0)
                    throw new Error("Maximum lock count exceeded");
                // 更新锁计数
                setState(nextc);
                return true;
            }
            return false;
        }
    } 
}
```