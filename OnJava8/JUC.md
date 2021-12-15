# JUC

## 线程进程

进程: 程序的执行过程叫做进程, 是系统分配资源的基本单位

线程: 是进程的一条执行路径, 是CPU调度的基本单位

多线程并发用于提高CPU利用率:

1. IO, 网络,硬件响应等操作很慢, CPU 空闲实现太多, 用多线程并发去提高CPU利用率
2. 线程过于重,创建,销毁,上下文切换的开销太大, 所以不用进程.



## 启动线程

使用 `thread.start()`, 告知系统以线程方式运行 `Thread::run()`

1. 使用 Runnable

```java
public class HelloRunnable implements Runnable {

public void run() {
System.out.println("Hello from a thread!");
}

public static void main(String args[]) {
(new Thread(new HelloRunnable())).start();
}

}
```

2.  继承Thread

```java
public class HelloThread extends Thread {

    public void run() {
        System.out.println("Hello from a thread!");
    }

    public static void main(String args[]) {
        (new HelloThread()).start();
    }

}
```



## 使用 Thread.sleep(long time) 暂停线程

Thread 的静态方法, 暂停当前线程, 单位毫秒.

睡眠的线程会被其他线程 打断--interrupt, 使用 `Thread.sleep(long time)` 时 必须捕捉 `InterruptedException`



## Interrupts 打断线程

线程被打断语义上意味着, 线程需要停下当前的工作, 而去处理别的事情, 程序员可以定义当线程被打断时应该做些什么, 但是绝大多数情况下, 对 Interrupts 的反应都是结束该线程.

### 打断标志 相关方法

`thread.interrupt()`是一个非静态方法. 当被打断的线程处于阻塞状态时, 即 wait(), sleep(), join() 时, 会清除打断标志(*Interrupt Status Flag*), 并抛出 `InterruptedException`. 对于不处于阻塞状态的线程, 会设置线程的打断标志, 供线程决定如何处理, 一般情况下, 程序员应该设置线程检测到自身打断标志后释放资源并结束. 这个方法对于线程被在 nio 组件管理时有其他处理, 留在 nio 包再探究.

静态方法 `Thread.interrupted()`返回当前线程的打断标志, 并清理打断标志

非静态方法 `thread.isInterrupted()`, 只检查目标线程的打断标志, 对打断标志无影响.



## join 方法

`t.join()` 方法令当前线程挂起, 等待 t 线程结束后再继续执行

```java
public class JoinTest {

    public static void main(String[] args) throws InterruptedException {
        Thread t = new Thread(
                ()->{
                    for (int i = 0; i < 10; i++) {
                        try {
                            Thread.sleep(1000);
                            System.out.println("inner sleep " + i + " seconds");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("inner finished");
                }
        );

        t.start();

        for (int i = 0; i < 3; i++) {
            Thread.sleep(1000);
            System.out.println("outer sleep " + i + " seconds");
            if(i == 1){
                System.out.println("inner join");
                t.join();
            }
        }
        System.out.println("outer finished");
    }
}

```



## 为何要线程同步

线程同步为了解决 

- 线程间相互干扰
- 内存数据不一致问题 

==最终的目的是 达成不同线程对同一个共享对象操作的可见性. 建立 happens-before 关系== 



## synchronized 最简单的线程同步方式

用 `synchronized` 关键字修饰方法, 使得方法在同一时刻只能有有个线程执行

其原理为对 Object 加锁, Object 的对象头中锁标记字段从无锁状态--偏向锁--自旋锁---重量级锁

无锁: 无记录

偏向锁: 记录所属线程

自旋锁: CAS

重量级锁: monitor enter / monitor exit 底层的 mutex 锁



## Atomic Access

对 `volatile` 修饰的变量的读和写操作是可以认为是原子的, 即在读的过程完成前,不接受写操作, 在写的过程完成前不接受读操作. 即,能够对读写建立 happens-before 关系, 禁止读写指令重排序.



## 使用 Immutable Object 避免内存不一致问题

## Lock Interface

[ReentrantLock](../../../../java/util/concurrent/locks/ReentrantLock.html): 

 - lock()
 - tryLock()
	- unlock()

[ReentrantReadWriteLock](../../../../java/util/concurrent/locks/ReentrantReadWriteLock.html):

- readLock().lock() / unlock() / tryLock()
- writeLock().lock() / unlock() / tryLock()

[StampedLock](../../../../java/util/concurrent/locks/StampedLock.html): 

```java
// 写锁的获取与释放
long stamp =  lock.writeLock();
lock.unlockWrite(stamp);

// 获取乐观读锁, 并验证
long stamp = sl.tryOptimisticRead();
if (!sl.validate(stamp)) {
    // 获取悲观读锁, 之后释放
    stamp = sl.readLock();
    try {
        currentX = x;
        currentY = y;
    } finally {
        sl.unlockRead(stamp);
    }
}

```

[LockSupport](../../../../java/util/concurrent/locks/LockSupport.html): LockSupport 方法均为 静态方法, park() 阻塞时不释放锁

- park()
- unpark(Thread thread)

## Executor 接口 分离 线程和实际业务






## 线程的状态

- NEW
- RUNNABLE
- BLOCKED
- WAITING
- TIMED_WAITING
- TERMINATED

## wait() 和 sleep() 区别

wait() 是 Object 的方法, 释放锁, 需要在同步块内使用, 由notify(), notifyAll() 唤醒

sleep() 是 Thread 的方法, 不释放锁

## [Synchronize](/JavaCurrent/java并发.md) 

## Lock

lock包下的类

lock.lock() 加锁

lock.unlock() 解锁

常用实现类