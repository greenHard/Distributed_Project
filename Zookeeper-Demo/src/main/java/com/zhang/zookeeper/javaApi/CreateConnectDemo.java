package com.zhang.zookeeper.javaApi;

import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

/**
 * 创建连接
 */
public class CreateConnectDemo {
    /**
     * 集群连接地址
     */
    public static final String CONNECT_STRING ="192.168.0.104:2181,192.168.0.105:2181,192.168.0.106:2181";

    /**
     * 计数器
     */
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws InterruptedException, IOException {
        ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING,5000,(event)->{
            // watcher 回调,当连接成功的时候执行下面的逻辑
            // 如果当前连接状态是成功的,通过计数器计数
            if(Watcher.Event.KeeperState.SyncConnected.equals(event.getState())){
                // 计数
                countDownLatch.countDown();
            }
        });
        countDownLatch.await();
        System.out.println("连接状态: " + zooKeeper.getState());
    }
}
