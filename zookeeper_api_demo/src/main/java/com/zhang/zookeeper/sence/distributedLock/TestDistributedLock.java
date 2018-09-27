package com.zhang.zookeeper.sence.distributedLock;

import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

/**
 * 测试分布式锁
 */
public class TestDistributedLock {
    public static void main(String[] args) {
        // zkClient 1
        final ZkClientExt zkClientExt1 = new ZkClientExt("10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181", 5000, 5000, new BytesPushThroughSerializer());
        final SimpleDistributedLockMutex mutex1 = new SimpleDistributedLockMutex(zkClientExt1, "/Mutex");

        // zkClient 2
        final ZkClientExt zkClientExt2 = new ZkClientExt("10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181", 5000, 5000, new BytesPushThroughSerializer());
        final SimpleDistributedLockMutex mutex2 = new SimpleDistributedLockMutex(zkClientExt2, "/Mutex");

        try {
            mutex1.acquire();
            System.out.println("Client1 locked");
            Thread client2Thd = new Thread(() -> {
                try {
                    mutex2.acquire();
                    System.out.println("Client2 locked");
                    mutex2.release();
                    System.out.println("Client2 released lock");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            client2Thd.start();
            Thread.sleep(5000);
            mutex1.release();
            System.out.println("Client1 released lock");
            client2Thd.join();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
