package com.zhang.zookeeper.curator.leader;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderSelector;
import org.apache.curator.framework.recipes.leader.LeaderSelectorListener;
import org.apache.curator.framework.state.ConnectionState;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.leader.LeaderElectionDemo
 * @Description: leader election
 * 通过LeaderSelectorListener可以对领导权进行控制， 在适当的时候释放领导权，这样每个节点都有可能获得领导权。
 * 而LeaderLatch则一直持有leadership， 除非调用close方法，否则它不会释放领导权。
 * @create 2018/09/25 17:13
 */
public class LeaderElectionDemo {

    /**
     * 客户端个数
     */
    private static final int CLIENT_QTY = 10;

    /**
     * 路径
     */
    private static final String PATH = "/demo/election";

    /**
     * zookeeper 集群
     */
    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";


    public static void main(String[] args) throws InterruptedException {

        for (int i = 0; i < CLIENT_QTY; i++) {
            CuratorFramework client = CuratorFrameworkFactory.newClient(CONNECT_STRING, new ExponentialBackoffRetry(2000, 2));
            client.start();
            final String name = "client#" + i;
            LeaderSelector leaderSelector = new LeaderSelector(client, PATH, new LeaderSelectorListener() {
                @Override
                public void takeLeadership(CuratorFramework client) throws Exception {
                    System.out.println(name + ": I am leader.");
                    Thread.sleep(2000);
                }

                @Override
                public void stateChanged(CuratorFramework client, ConnectionState newState) {

                }
            });
            leaderSelector.autoRequeue();
            leaderSelector.start();
        }

        Thread.sleep(Integer.MAX_VALUE);
    }
}
