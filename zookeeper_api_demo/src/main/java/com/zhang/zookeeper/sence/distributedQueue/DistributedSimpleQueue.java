package com.zhang.zookeeper.sence.distributedQueue;

import org.I0Itec.zkclient.ExceptionUtil;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * 简单分布式队列
 */
public class DistributedSimpleQueue<T> {

    // zk 客户端
    final ZkClient zkClient;

    // queue节点
    final String root;

    private static final String NODE_NAME = "n_";

    DistributedSimpleQueue(ZkClient zkClient, String root) {
        this.zkClient = zkClient;
        this.root = root;
    }

    // 判断队列大小
    public int size() {
        return zkClient.getChildren(root).size();
    }

    // 判断队列是否为空
    public boolean isEmpty() {
        return zkClient.getChildren(root).size() == 0;
    }

    // 向队列提供数据
    boolean offer(T element) throws Exception{

        // 创建顺序节点
        String nodeFullPath = root.concat("/").concat(NODE_NAME);
        System.out.println("before create , the path is " + nodeFullPath);
        try {
            String persistentSequential = zkClient.createPersistentSequential(nodeFullPath, element);
            System.out.println("after create , the path is " + persistentSequential);
        } catch (ZkNoNodeException e) {
            // 节点不存在,先创建父节点
            zkClient.createPersistent(root);
            offer(element);
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }

        return true;
    }


    // 从队列取数据
    public T poll() throws Exception{
        try {
            // 获取所有顺序节点
            List<String> list = zkClient.getChildren(root);

            if (list.size() == 0) {
                return null;
            }

            // 排序
            list = list.stream().sorted(Comparator.comparing(DistributedSimpleQueue::getNodeNumber)).collect(toList());

            // 循环每个顺序节点名
            for (String nodeName : list) {
                // 构造出顺序节点的完整路径
                String nodeFullPath = root.concat("/").concat(nodeName);
                try {
                    // 读取顺序节点的内容
                    T node = zkClient.readData(nodeFullPath);
                    // 删除顺序节点
                    zkClient.delete(nodeFullPath);
                    return node;
                } catch (ZkNoNodeException e) {
                    // ignore 由其他客户端把这个顺序节点消费掉了
                }
            }

            return null;
        } catch (Exception e) {
            throw ExceptionUtil.convertToRuntimeException(e);
        }
    }

    // 获取节点名称
    private static String getNodeNumber(String str) {
        int index = str.lastIndexOf(DistributedSimpleQueue.NODE_NAME);
        if (index >= 0) {
            index += NODE_NAME.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }


}
