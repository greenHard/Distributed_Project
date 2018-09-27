package com.zhang.zookeeper.sence.namingService;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.BytesPushThroughSerializer;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 命名服务
 * 利用zookeeper顺序节点的特性，制作分布式的ID生成器，写过数据库应用的朋友都知道，我们在往数据库表中插入记录时，通常需要为该记录创建唯一的ID，
 * 在单机环境中我们可以利用数据库的主键自增功能。但在分布式环境则无法使用，有一种方式可以使用UUID，但是它的缺陷是没有规律，很难理解。
 * 利用zookeeper顺序节点的特性，我们可以生成有顺序的，容易理解的，同时支持分布式环境的序列号。
 */
public class IdMaker {
    private ZkClient client;

    // zk
    private final String server = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";

    // zookeeper顺序节点的父节点
    private final String root = "/NameService/IdGen";

    // 顺序节点的名称
    private final String nodeName = "ID";

    private volatile boolean running = false;

    private ExecutorService service;

    public enum RemoveMethod {
        NONE, IMMEDIATELY, DELAY
    }

    // 开启服务
    public void start() throws Exception {
        if (running)
            throw new Exception("server has stated...");
        running = true;
        init();
    }

    // 初始化
    private void init() {
        client = new ZkClient(server, 5000, 5000, new BytesPushThroughSerializer());
        service = Executors.newFixedThreadPool(10);
        client.createPersistent(root, true);
    }

    // 停止服务
    void stop() throws Exception {
        if (!running)
            throw new Exception("server has stopped...");
        running = false;
        freeResource();
    }

    // 释放资源
    private void freeResource() {
        // 释放连接池
        service.shutdown();
        try {
            service.awaitTermination(2, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            service = null;
        }
        if (client != null) {
            client.close();
            client = null;
        }
    }

    // 生成ID
    String generateId(RemoveMethod removeMethod) throws Exception {
        checkRunning();

        // 构造顺序节点的完整路径
        final String fullNodePath = root.concat("/").concat(nodeName);

        // 创建持久化有序节点
        final String outPath = client.createPersistentSequential(fullNodePath, null);

        System.out.println(outPath);

        // 避免zookeeper节点的数据暴增,直接删除掉刚创建的顺序节点
        if (RemoveMethod.IMMEDIATELY.equals(removeMethod)) {
            // 立即删除
            client.delete(outPath);
        } else if (RemoveMethod.DELAY.equals(removeMethod)) {
            // 延迟删除
            // 用线程池执行删除，让generateId()方法尽快返回
            service.execute(() -> client.delete(outPath));
        } else if (RemoveMethod.NONE.equals(removeMethod)) {

        }

        // node-0000000000, node-0000000001
        return extractId(outPath);
    }

    // 检测容器是否启动
    private void checkRunning() throws Exception {
        if (!running)
            throw new Exception("server not started..");
    }

    // 从顺序节点名中提取我们要的ID值
    private String extractId(String path) {
        // NameService/IdGen/ID0000000000
        int index = path.lastIndexOf(nodeName);
        if (index >= 0) {
            index += nodeName.length();
            return index <= path.length() ? path.substring(index) : "";
        }
        return path;
    }


}
