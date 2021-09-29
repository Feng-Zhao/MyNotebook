package MyNotebook.OnJava8.JavaCurrent.demo;

import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;



public class AQSDemo {
    static class Node {
        String id;
        String state;
        
        Node(String id,String state){
            this.id = id;
            this.state = state;
        }

        Node(String id){
            this.id = id;
            this.state = "初始化完成";
        }

        void deal(){
            this.state = "处理完成";
        }

        void finish(){
            this.state = "已释放";
        }

        void showState(){
            System.out.println("当前 id: " + id + ", state 为: " + state);
        }
        
    }


    static class MyRunnable implements Runnable{
        Node myNode;
        String id;
        ConcurrentLinkedQueue<Node> clq;
        CountDownLatch init;
        CountDownLatch deal;
        CountDownLatch distory;


        static Random random = new Random();

        MyRunnable(Node node, ConcurrentLinkedQueue<Node> q){
            this.id = node.id;
            this.clq = q;
        }
        MyRunnable(String id,ConcurrentLinkedQueue<Node> q){
            myNode = new Node(id);
            this.id = myNode.id;
            clq = q;
        }
        MyRunnable(ConcurrentLinkedQueue<Node> q, CountDownLatch init, CountDownLatch deal , CountDownLatch distory){
            clq = q;
            myNode = q.poll();
            this.id = myNode.id;

            this.init = init;
            this.deal = deal;
            this.distory = distory;
        }

        @Override
        public void run() {
            int i = random.nextInt(10);
            System.out.println("线程 " + id + " 处理Node," + " Node " + myNode.id + "state 为： " + myNode.state +  " 休眠 " + i + "秒");
            init.countDown();

            try{
                Thread.sleep( 1000 * i );
            }catch (InterruptedException e){
                e.printStackTrace();
                System.err.println("线程 " + id + " 捕获 InterruptedException");
            }

            myNode.deal();
            System.out.println("线程 " + id + " 处理Node," + " Node " + myNode.id + "state 为： " + myNode.state +  " 休眠 " + i + "秒");
            deal.countDown();

            try{
                Thread.sleep( 1000 * i );
            }catch (InterruptedException e){
                e.printStackTrace();
                System.err.println("线程 " + id + " 捕获 InterruptedException");
            }

            myNode.finish();
            System.out.println("线程 " + id + " 处理Node," + " Node " + myNode.id + "state 为： " + myNode.state +  " 休眠 " + i + "秒");
            distory.countDown();

            try{
                Thread.sleep( 1000 * i );
            }catch (InterruptedException e){
                e.printStackTrace();
                System.err.println("线程 " + id + " 捕获 InterruptedException");
            }

            System.out.println("线程 " + id + " 结束");

        }
        
    }
    public static void main(String[] args) {
        
        ConcurrentLinkedQueue<Node> clq = new ConcurrentLinkedQueue<>();
        CountDownLatch init_latch = new CountDownLatch(10);
        CountDownLatch deal_latch = new CountDownLatch(10);
        CountDownLatch distory_latch = new CountDownLatch(10);

        for (int id = 0; id < 10; id++) {
            Node n = new Node(Integer.toString(id));
            clq.offer(n);
        }
        System.out.println("========== 队列装载完毕 ==============");
        for(int i = 0; i < 10; i++){
            Thread t = new Thread(new MyRunnable(clq,init_latch,deal_latch,distory_latch));
            t.start();
        }

        AQSDemo demo = new AQSDemo();
        Thread init = new Thread(demo.new LatchRunnable(init_latch, "Node初始化"));
        init.start();
        

    }

    private class LatchRunnable implements Runnable{
        CountDownLatch latch;
        String name;
        LatchRunnable(CountDownLatch latch, String name){
            this.latch = latch;
            this.name = name; 
        }
        @Override
        public void run() {
            try{
                latch.await();
                System.out.println("================" + name +  " 完成 ====================");
            }catch (InterruptedException e){
                e.printStackTrace();
            }
        }
    }
}