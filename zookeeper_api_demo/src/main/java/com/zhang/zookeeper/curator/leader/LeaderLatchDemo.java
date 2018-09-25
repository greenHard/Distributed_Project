package com.zhang.zookeeper.curator.leader;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.leader.LeaderLatchDemo
 * @Description: LeaderLatch实例可以增加ConnectionStateListener来监听网络连接问题。 当 SUSPENDED 或 LOST 时, leader不再认为自己还是leader.
 * 当LOST 连接重连后 RECONNECTED,LeaderLatch会删除先前的ZNode然后重新创建一个.
 * LeaderLatch用户必须考虑导致leader丢失的连接问题。 强烈推荐你使用ConnectionStateListener。
 * @create 2018/09/25 14:51
 */
public class LeaderLatchDemo {
    /**
     * 客户端个数
     */
    private static final int CLIENT_QTY = 10;

    /**
     * 路径
     */
    private static final String PATH = "/demo/leader";

    /**
     * zookeeper 集群
     */
    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";


    public static void main(String[] args) throws Exception {
        // 客户端
        List<CuratorFramework> clients = new ArrayList<>();

        // leaderLatch
        List<LeaderLatch> leaderLatchList = new ArrayList<>();

        for (int i = 0; i < CLIENT_QTY; ++i) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(CONNECT_STRING, new ExponentialBackoffRetry(1000, 3));
            clients.add(client);
            // 启动client
            client.start();
            final LeaderLatch leaderLatch = new LeaderLatch(client, PATH, "Client #" + i);
            // 添加监听器
            leaderLatch.addListener(new LeaderLatchListener() {
                @Override
                public void isLeader() {
                    System.out.println(leaderLatch.getId() + ":I am leader. I am doing jobs!");
                    try {
                        // 暂停10秒
                        TimeUnit.SECONDS.sleep(10);
                        System.out.println("=======================");
                        // 模拟异常,查看是否重新选举
                        leaderLatch.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    System.out.println("success");
                }

                @Override
                public void notLeader() {
                    System.out.println(leaderLatch.getId() + ":I am not leader. I will do nothing!");
                }
            });
            leaderLatchList.add(leaderLatch);
            leaderLatch.start();
        }
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }
}
