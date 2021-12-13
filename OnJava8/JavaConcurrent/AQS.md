## AQS

内部维护一个 FIFO 双向队列 volatile int state 标识 STATE
队列内结构为 Node: volatile waitStatus 标识 线程状态， volatile thread 表示关联线程， volatile Node prev,next 为前驱/后继节点, Node nextWaiter 表示下一条件等待节点

### Node 简化结构

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

### AQS 简化结构

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

### AQS 流程详解

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

### AQS 流程总结

```puml
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