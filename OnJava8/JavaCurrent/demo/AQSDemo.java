package MyNotebook.OnJava8.JavaCurrent.demo;

import java.rmi.server.ExportException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;



public class AQSDemo extends AbstractQueuedSynchronizer {
    private static int i = 0;
    private static int count = 0;

    @Override
    protected boolean isHeldExclusively() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected boolean tryAcquire(int arg) {
        if (arg == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected int tryAcquireShared(int arg) {
        if (arg == 1) {
            return 1;
        } else {
            return -1;
        }
    }

    @Override
    protected boolean tryRelease(int arg) {
        if (arg == 1) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected boolean tryReleaseShared(int arg) {
        if (arg == 1) {
            return true;
        } else {
            return false;
        }
    }

    public static void main(String[] args) {
        AQSDemo aqs = new AQSDemo();
        ReentrantLock lock = new ReentrantLock();

        Thread[] tList = new Thread[10];
        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(new Runnable(){
                @Override
                public void run() {
                    int id = AQSDemo.i++;
                    while(AQSDemo.count <= 11){
                        lock.lock();
                        AQSDemo.count++;
                        System.out.println("Thread:"+id + "\t Count: " + AQSDemo.count);
                        lock.unlock();
                    }
                }
                
            });
            tList[i] = t;
            t.start();
        }

        for (int i = 0; i < 10; i++) {
            
        }
    }

}