## ReentrantLock

### Lock 接口

```java
void lock();
boolean tryLock();
boolean tryLock(long time, TimeUnit unit) throws InterruptedException;
void lockInterruptibly() throws InterruptedException;
void unlock();
Condition newCondition();
```

### ReentrantLock

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