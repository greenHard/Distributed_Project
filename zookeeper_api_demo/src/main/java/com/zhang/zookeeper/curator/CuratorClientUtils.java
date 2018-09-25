package com.zhang.zookeeper.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.CuratorClientUtils
 * @Description: curator工具类
 * @create 2018/09/25 11:08
 */
public class CuratorClientUtils {

    /**
     * zookeeper 集群
     */
    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";

    /**
     * 创建curator实例
     */
    public static CuratorFramework getInstance() {
        CuratorFramework curatorFramework = CuratorFrameworkFactory.builder()
                .connectString(CONNECT_STRING)
                .sessionTimeoutMs(3000)
                .connectionTimeoutMs(3000)
                .retryPolicy(new ExponentialBackoffRetry(2000, 2))
                .build();
        // 启动客户端
        curatorFramework.start();
        return curatorFramework;

    }
}
