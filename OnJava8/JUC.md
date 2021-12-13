# JUC

## 线程进程

进程: 程序的执行过程叫做进程, 是系统分配资源的基本单位

线程: 是进程的一条执行路径, 是CPU调度的基本单位

多线程并发用于提高CPU利用率:

1. IO, 网络,硬件响应等操作很慢, CPU 空闲实现太多, 用多线程并发去提高CPU利用率
2. 线程过于重,创建,销毁,上下文切换的开销太大, 所以不用进程.

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