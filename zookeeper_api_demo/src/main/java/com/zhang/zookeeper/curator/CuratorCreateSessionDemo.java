package com.zhang.zookeeper.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.CuratorCreateSessionDemo
 * @Description: curator创建连接
 * @create 2018/09/25 13:01
 */
public class CuratorCreateSessionDemo {

    /**
     * zookeeper 集群
     */
    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";

    public static void main(String[] args) {
        // 创建会话的两种方式
        // normal
        CuratorFramework curatorFramework = CuratorFrameworkFactory.newClient(CONNECT_STRING, 3000, 3000, new ExponentialBackoffRetry(2000, 2));
        curatorFramework.start();

        // fluent风格
        CuratorFramework curatorFramework1 = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .sessionTimeoutMs(3000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(2000, 2))
                .build();
        curatorFramework1.start();
        System.out.println("success");


    }
}
