package com.zhang.zookeeper.javaApi.acl;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 *
 * @author adai
 * @since 20170912 10:47
 * 拥有某种权限的zookeeper的客户端(权限类型为:digest 所对应的具体权限:adai【类型用户名密码验证等】)
 */
public class AuthZookeeper {
    private CountDownLatch countDownLatch = new CountDownLatch(1);
    private ZooKeeper zoo = null ;
    public AuthZookeeper(){
        try {
            zoo = new ZooKeeper(ZookeeperUtil.CONNECT_ADDR,
                    ZookeeperUtil.SESSION_TIMEOUT,
                    new ZookeeperWatcher(countDownLatch,"AuthZookeeper"));
            //授权  参数1：权限类型    参数2:对应的正确权限（CorrectAuthZookeeper创建的节点权限与其一致）
            zoo.addAuthInfo(ZookeeperUtil.AUTH_TYPE, ZookeeperUtil.CORRECTAUTH.getBytes());
            countDownLatch.await();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 释放连接
     */
    public void close(){
        if(zoo != null ){
            try {
                zoo.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    public ZooKeeper getCorrectAuthZookeeper(){
        return zoo;
    }

    /**
     * 创建一个具有认证权限的节点
     * @param path
     * @param data
     */
    public void createAuthNode(String path , String data){
        try {
            Stat stat = zoo.exists(path, false);
            if (stat != null){
                System.out.println(path+"节点已经存在:");
                return ;
            }
        } catch (Exception e) {
        }
        List<ACL> acls = new ArrayList<ACL>(1);
        for (ACL ids_acl : Ids.CREATOR_ALL_ACL) { //遍历出所有权限类型
            acls.add(ids_acl);
        }
        ZookeeperUtil.createAuthNode(zoo , path, data, acls, CreateMode.PERSISTENT ,"AuthZookeeper");
    }
}
