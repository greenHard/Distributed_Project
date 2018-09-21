package com.zhang.zookeeper.javaApi;

import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.apache.zookeeper.CreateMode.PERSISTENT;

/**
 * Api操作，要实现持续监听,watcher是一次性的
 */
public class ApiOperatorDemo implements Watcher {
    /**
     * 集群连接地址
     */
    public static final String CONNECT_STRING = "192.168.0.107:2181,192.168.0.108:2181,192.168.0.105:2181";

    /**
     * 计数器
     */
    private static final CountDownLatch countDownLatch = new CountDownLatch(1);

    public static void main(String[] args) throws IOException, InterruptedException, KeeperException {
        ApiOperatorDemo apiOperatorDemo = new ApiOperatorDemo();
        Stat stat = new Stat();
        ZooKeeper zooKeeper = new ZooKeeper(CONNECT_STRING, 5000, apiOperatorDemo);
        countDownLatch.await();
        String path = "/hello";
        String value = "world";

        // NodeCreated 通过exists API设置
        Stat resultStat = zooKeeper.exists(path, apiOperatorDemo);
        if (resultStat == null) {
            // 创建节点
            String result = zooKeeper.create(path, value.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
            byte[] data = zooKeeper.getData(path, apiOperatorDemo, stat);
            System.out.println("创建节点成功: " + result + ",获取的数据为：" + new String(data));
        }

        // 对于NodeDeleted 通过exists 和 getData()设置
        // 删除节点
        // zooKeeper.delete(path,-1);

        // 对于NodeDataChanged 通过exists或getData设置
        // 节点数据改变
        if (zooKeeper.exists(path, apiOperatorDemo) != null) {
            Stat updateStat = zooKeeper.setData(path, "zookeeper".getBytes(), -1);
            System.out.println("更新的stat," + updateStat);
        }

        // 对于NodeChildrenChanged 通过getChildren设置
        String path1 = "/node2";
        // 创建一个永久节点,永久节点才有子节点
        zooKeeper.create(path1, "123".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, PERSISTENT);
        TimeUnit.SECONDS.sleep(2);
        Stat exists = zooKeeper.exists("/node2/node1", apiOperatorDemo);
        if (exists == null) {
            List<String> children = zooKeeper.getChildren("/node2", apiOperatorDemo);
            System.out.println("子节点的数据: " + children);

            String createResult = zooKeeper.create("/node2/node1", "1234".getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, PERSISTENT);
            System.out.println("创建子节点结果: " + createResult);
        }
    }

    @Override
    public void process(WatchedEvent event) {
        // watcher 回调,当连接成功的时候执行下面的逻辑
        // 如果当前连接状态是成功的,通过计数器计数
        if (Watcher.Event.KeeperState.SyncConnected == event.getState()) {
            System.out.println("------------");
            if (Watcher.Event.EventType.None == event.getType()) {
                countDownLatch.countDown();
                System.out.println(event.getState() + "-->" + event.getType());
                // None状态
            } else if (Watcher.Event.EventType.NodeCreated == event.getType()) {
                System.out.println("节点创建了...." + event.getPath());
                // 节点创建
            } else if (Watcher.Event.EventType.NodeDataChanged == event.getType()) {
                System.out.println("节点数据改变了..." + event.getPath());
                // 节点数据改变
            } else if (Watcher.Event.EventType.NodeChildrenChanged == event.getType()) {
                System.out.println("子节点改变了..." + event.getPath());
                // 子节点改变
            } else if (Watcher.Event.EventType.NodeDeleted == event.getType()) {
                System.out.println("节点删除了..." + event.getPath());
                // 节点删除
            }
        }
    }
}
