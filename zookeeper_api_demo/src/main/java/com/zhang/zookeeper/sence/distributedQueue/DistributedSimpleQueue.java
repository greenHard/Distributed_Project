package com.zhang.zookeeper.sence.distributedQueue;

import org.I0Itec.zkclient.ZkClient;

/**
 * 简单分布式队列
 */
public class DistributedSimpleQueue<T> {

    protected final ZkClient zkClient;

    // queue节点
    protected final String root;

    protected static final String NODE_NAME =  "n_";

    public DistributedSimpleQueue(ZkClient zkClient,String root){
        this.zkClient = zkClient;
        this.root = root;
    }
}
