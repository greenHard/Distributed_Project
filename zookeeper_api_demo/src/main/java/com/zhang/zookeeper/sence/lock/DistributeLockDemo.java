package com.zhang.zookeeper.sence.lock;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.lock.DistributeLockDemo
 * @Description: 分布式锁Demo
 * @create 2018/09/26 9:41
 */
public class DistributeLockDemo {

    /**
     * ZooKeeper 锁节点路径, 分布式锁的相关操作都是在这个节点上进行
     */
    private final String lockPath = "/distributeLock";

    /**
     * ZooKeeper 服务地址, 单机格式为:(127.0.0.1:2181), 集群格式为:(127.0.0.1:2181,127.0.0.1:2182,127.0.0.1:2183)
     */
    private String connectString;

    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy;

    /**
     * 客户端对象
     */
    private CuratorFramework client;

    /**
     * 其他客户端对象
     */
    private CuratorFramework client2;

    /**
     * 初始化方法
     */
    @Before
    public void init() {
        // 设置 ZooKeeper 集群地址
        connectString = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";
        // 重试策略
        // 初始休眠时间为 1000ms, 最大重试次数为 3
        retryPolicy = new ExponentialBackoffRetry(1000, 3);
        // 创建一个客户端, 60000(ms)为 session 超时时间, 15000(ms)为链接超时时间
        client = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retryPolicy);
        client2 = CuratorFrameworkFactory.newClient(connectString, 60000, 15000, retryPolicy);
        // 创建会话
        client.start();
        client2.start();
    }


    /**
     * 释放资源
     */
    @After
    public void close() {
        CloseableUtils.closeQuietly(client);
    }

    /**
     * 共享锁 -- 不可从入锁
     */
    @Test
    public void sharedLock() throws Exception {
        // 创建共享锁
        InterProcessLock lock = new InterProcessSemaphoreMutex(client, lockPath);

        // lock2 用于模拟其他客户端
        InterProcessLock lock2 = new InterProcessSemaphoreMutex(client2, lockPath);

        // 获取锁对象
        lock.acquire();

        // 测试是否可以重入
        // 超时获取锁对象(第一个参数为时间, 第二个参数为时间单位), 因为锁已经被获取, 所以返回 false
        assertFalse(lock.acquire(20, TimeUnit.SECONDS));

        // 释放锁
        lock.release();

        // lock2 尝试获取锁成功, 因为锁已经被释放
        assertTrue(lock2.acquire(2, TimeUnit.SECONDS));
        lock2.release();
    }

    /**
     * 共享可重入锁
     */
    @Test
    public void sharedReentrantLock() throws Exception {
        // 创建可重入锁
        InterProcessLock lock = new InterProcessMutex(client, lockPath);

        // lock2模拟其他客户端
        InterProcessLock lock2 = new InterProcessMutex(client2, lockPath);

        // lock 获取锁
        lock.acquire();

        try {
            // lock 第二次获取锁
            lock.acquire();
            try {
                // lock2 超时获取锁, 因为锁已经被 lock 客户端占用, 所以获取失败, 需要等 lock 释放
                assertFalse(lock2.acquire(30, TimeUnit.SECONDS));
            } finally {
                lock.release();
            }
        } finally {
            // 重入锁获取与释放需要一一对应, 如果获取 2 次, 释放 1 次
            // 那么该锁依然是被占用, 如果将下面这行代码注释, 那么会发现下面的 lock2 获取锁失败
            lock.release();
        }

        // 在 lock 释放后, lock2 能够获取锁
        assertTrue(lock2.acquire(2, TimeUnit.SECONDS));
        lock2.release();
    }


    /**
     * 共享可重复读写锁 -- 读取数据线程总是能看到最新写入的数据
     * 公平锁（Fair）：加锁前检查是否有排队等待的线程，优先排队等待的线程，先来先得
     * 非公平锁（noFair）：加锁时不考虑排队等待问题，直接尝试获取锁，获取不到自动到队尾等待
     * 非公平锁性能比公平锁高5~10倍，因为公平锁需要在多核的情况下维护一个队列
     * 首先Java中的ReentrantLock 默认的lock()方法采用的是非公平锁。
     */
    @Test
    public void sharedReentrantReadWriteLock() throws Exception {

        // 创建读写锁对象, Curator以公平锁的方式进行实现
        InterProcessReadWriteLock lock = new InterProcessReadWriteLock(client, lockPath);

        // lock2 用于模拟其他客户端
        InterProcessReadWriteLock lock2 = new InterProcessReadWriteLock(client2, lockPath);

        // 使用 lock 模拟读操作
        // 使用 lock2 模拟写操作
        // 获取读锁(使用 InterProcessMutex 实现, 所以是可以重入的)
        InterProcessMutex readLock = lock.readLock();

        // 获取写锁(使用 InterProcessMutex 实现, 所以是可以重入的)
        InterProcessMutex writeLock = lock2.writeLock();

        // 读写锁测试对象
        class ReadWriteLockTest {

            // 测试数据变更字段
            private Integer testData = 0;

            private Set<Thread> threadSet = new HashSet<>();

            // 写入数据
            private void write() throws Exception {
                writeLock.acquire();
                try {
                    Thread.sleep(1000);
                    testData++;
                    System.out.println("写入数据" + testData);
                } finally {
                    writeLock.release();
                }
            }

            // 读取数据
            private void read() throws Exception {
                readLock.acquire();
                try {
                    Thread.sleep(1000);
                    System.out.println("读取数据" + testData);
                } finally {
                    readLock.release();
                }
            }

            // 等待线程结束, 防止 test 方法调用完成后, 当前线程直接退出, 导致控制台无法输出信息
            private void waitThread() throws InterruptedException {
                for (Thread thread : threadSet) {
                    thread.join();
                }
            }

            // 创建线程方法
            private void createThread(int type) {
                Thread thread = new Thread(() -> {
                    try {
                        if (type == 1) {
                            write();
                        } else {
                            read();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
                threadSet.add(thread);
                thread.start();
            }

            // 测试方法
            private void test() {
                for (int i = 0; i < 5; i++) {
                    createThread(1);
                }
                for (int i = 0; i < 5; i++) {
                    createThread(2);
                }
            }
        }

        ReadWriteLockTest readWriteLockTest = new ReadWriteLockTest();
        readWriteLockTest.test();
        readWriteLockTest.waitThread();
    }

    /**
     * 多重共享锁
     */
    @Test
    public void multiLock() throws Exception {
        // 可重入锁
        InterProcessLock interProcessLock1 = new InterProcessMutex(client, lockPath);
        // 不可重入锁
        InterProcessLock interProcessLock2 = new InterProcessSemaphoreMutex(client2, lockPath);
        // 创建多重锁对象
        InterProcessLock lock = new InterProcessMultiLock(Arrays.asList(interProcessLock1, interProcessLock2));
        // 获取参数集合中的所有锁
        lock.acquire();

        // 因为存在一个不可重入锁, 所以整个 InterProcessMultiLock 不可重入
        assertFalse(lock.acquire(2, TimeUnit.SECONDS));
        // interProcessLock1 是可重入锁, 所以可以继续获取锁
        assertTrue(interProcessLock1.acquire(2, TimeUnit.SECONDS));
        // interProcessLock2 是不可重入锁, 所以获取锁失败
        assertFalse(interProcessLock2.acquire(2, TimeUnit.SECONDS));

        // 释放参数集合中的所有锁
        lock.release();

        // interProcessLock2 中的锁已经释放, 所以可以获取
        assertTrue(interProcessLock2.acquire(2, TimeUnit.SECONDS));

    }


}
