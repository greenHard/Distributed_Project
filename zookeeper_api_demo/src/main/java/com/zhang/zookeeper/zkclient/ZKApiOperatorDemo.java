package com.zhang.zookeeper.zkclient;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.TimeUnit;

/**
 * 使用zkclient 操作zookeeper 节点
 * zkClient的操作比zookeeper原本的api要简单的多
 */
public class ZKApiOperatorDemo {

    /**
     * 集群连接地址
     */
    // public static final String CONNECT_STRING = "192.168.0.101:2181,192.168.0.104:2181,192.168.0.105:2181";

    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";


    /**
     * 获取连接
     */
    private static ZkClient getConnect() {
        return new ZkClient(CONNECT_STRING, 2000);
    }

    public static void main(String[] args) throws InterruptedException {
        ZkClient zkClient = getConnect();

        // 递归创建父节点
        zkClient.createPersistent("/zkClient/childZkClient", true);
        System.out.println("create success..");

        // 递归删除节点
        zkClient.deleteRecursive("/zkClient");
        System.out.println("delete success..");

        zkClient.deleteRecursive("/node");

        zkClient.createPersistent("/node");

        // 监听watcher
        // 订阅节点数据改变
        zkClient.subscribeDataChanges("/node", new IZkDataListener() {
            @Override
            public void handleDataChange(String dataPath, Object data) throws Exception {
                System.out.println("/node 数据修改了...");
            }

            @Override
            public void handleDataDeleted(String dataPath) throws Exception {
                System.out.println("/node 数据删除了...");
            }
        });
        zkClient.writeData("/node", "zkClient write..");
        TimeUnit.SECONDS.sleep(2);
        zkClient.deleteRecursive("/node");
        TimeUnit.SECONDS.sleep(2);

        // 监听子节点
        zkClient.createPersistent("/node/children",true);
        zkClient.subscribeChildChanges("/node/children", (parentPath, currentChilds) -> System.out.println("子节点变了..."));
        zkClient.writeData("/node/children","123");
        TimeUnit.SECONDS.sleep(2);
        System.out.println("/node/children data change...");


        // 获取子节点
        //List<String> childrenList = zkClient.getChildren("/node");
        //System.out.println("node下面所有的子节点 ->" + childrenList);
    }


}
