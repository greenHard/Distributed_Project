package com.zhang.zookeeper.zkclient;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 使用zkClient 操作zookeeper 节点
 * zkClient的操作比zookeeper原本的api要简单的多
 */
public class ZKApiOperatorDemo {

    /**
     * 集群连接地址
     */
    // public static final String CONNECT_STRING = "192.168.0.101:2181,192.168.0.104:2181,192.168.0.105:2181";

    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";


    private ZkClient zkClient;


    /**
     * 会话创建
     */
    @Before
    public void createSession() {
        zkClient = new ZkClient(CONNECT_STRING, 2000, 2000, new SerializableSerializer());
    }


    /**
     * 释放资源
     */
    @After
    public void close(){
        zkClient.close();
    }

    /**
     * 节点创建
     */
    @Test
    public void createNode() {
        User u = new User();
        u.setAge(18);
        u.setName("test");
        String path = zkClient.create("/test", u, CreateMode.PERSISTENT);
        System.out.println("create path : " + path);
    }


    /**
     * 获取节点内容
     */
    @Test
    public void getData() {
        Stat stat = new Stat();
        User u = zkClient.readData("/test", stat);
        System.out.println("获得/test的数据为:" + u.toString());
        System.out.println("获得stat的状态为: " + stat);
    }

    /**
     * 获取子节点
     */
    @Test
    public void getChild() {
        List<String> children = zkClient.getChildren("/test");
        System.out.println("/test下面的子节点:" + children);
    }

    /**
     * 检测节点
     */
    @Test
    public void nodeExists() {
        boolean exists = zkClient.exists("/test");
        System.out.println("/test节点是否存在: " + exists);
    }


    /**
     * 修改节点数据
     */
    @Test
    public void writeNode() {
        User user = new User();
        user.setAge(20);
        user.setName("test2");
        // 使用版本号,乐观锁机制
        zkClient.writeData("/test", user, 0);
    }


    /**
     * 删除节点
     */
    @Test
    public void deleteNode() {
        // 递归删除
        boolean result = zkClient.deleteRecursive("/test");
        System.out.println("删除/test,结果: " + result);
    }


    /**
     * 订阅子节点列表变化
     */
    @Test
    public void subscribeChildChanges() throws InterruptedException {
        zkClient.subscribeChildChanges("/test20", new ZkChildListener());
        zkClient.createPersistent("/test20/child", true);
        TimeUnit.SECONDS.sleep(5);
        zkClient.deleteRecursive("/test20");
        TimeUnit.SECONDS.sleep(30);
    }


    /**
     * 订阅数据内容变化
     */
    @Test
    public void subscribeDataChange() throws InterruptedException {
        zkClient.subscribeDataChanges("/test20", new ZkDataListener());
        zkClient.createPersistent("/test20", true);
        User user = new User();
        user.setName("bill");
        user.setAge(25);
        zkClient.writeData("/test20",user );
        TimeUnit.SECONDS.sleep(5);
        zkClient.deleteRecursive("/test20");
        TimeUnit.SECONDS.sleep(10);
    }

    /**
     * 子节点列表边化
     */
    private static class ZkChildListener implements IZkChildListener {

        @Override
        public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
            System.out.println("父节点路径: " + parentPath);
            System.out.println("当前路径子节点:" + currentChilds);
        }
    }


    /**
     * 数据内容监听
     */
    private static class ZkDataListener implements IZkDataListener {

        @Override
        public void handleDataChange(String dataPath, Object data) throws Exception {
            System.out.println("数据改变, " + dataPath + ":" + data.toString());
        }

        @Override
        public void handleDataDeleted(String dataPath) throws Exception {
            System.out.println("数据删除: " + dataPath);
        }
    }



}
