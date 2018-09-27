package com.zhang.zookeeper.thread.lock;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.thread.lock.ReentrantLocKDemo
 * @Description: 重入锁Demo
 * @create 2018/09/25 18:33
 */
public class ReentrantLocKDemo implements Runnable{

    private synchronized void get(){
        System.out.println("get -> "+ Thread.currentThread().getName());
        set();
    }

    private synchronized void set() {
        System.out.println("set -> "+Thread.currentThread().getName());
    }


    @Override
    public void run() {
        get();
    }

    public static void main(String[] args) {
        /*
         * 广义上的可重入锁指的是可重复可递归调用的锁，在外层使用锁之后，在内层仍然可以使用，并且不发生死锁（前提得是同一个对象或者class)
         * 这样的锁就叫做可重入锁。ReentrantLock和synchronized都是可重入锁，下面是一个用synchronized实现的例子：
         * 递归使用没有发生死锁
         */
        while (true){
            ReentrantLocKDemo demo = new ReentrantLocKDemo();
            new Thread(demo).start();
        }
    }
}
