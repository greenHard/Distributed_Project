package com.zhang.zookeeper.javaApi.acl;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;

/**
 * 工具类,为了测试zookeeper权限(ACL),封装了zookeeper一些api
 *
 * @author adai
 * @since 20170913 01:28
 */
public class ZookeeperUtil {
    //连接地址
    public static final String CONNECT_ADDR = "192.168.0.107,192.168.0.108,192.168.0.105";
    //会话连接超时时间
    public static final int SESSION_TIMEOUT = 2000;
    //测试数据跟路径
    public static final String ROOT_PATH = "/testAuth";
    public static final String CHILDREN = "/testAuth/children";
    public static final String CHILDRENAUTH = "/testAuth/children2";
    //认证方式
    public static final String AUTH_TYPE = "digest";
    //正确的认证方法
    public static final String CORRECTAUTH = "adai";
    //错误的认证方法
    public static final String BADAUTH = "adai1";
    //计数器
    public static final AtomicInteger SEQ = new AtomicInteger();

    public static final String LOG_PRE_OF_MAIN = "【main】:";


    /**
     * 得到指定节点的数据内容
     *
     * @param zoo    zookeeper对象
     * @param path   节点的全路径
     * @param  zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void getData(ZooKeeper zoo, String path, String zookeeperCliName) {
        try {
            String data = new String(zoo.getData(path, false, null));
            System.out.println(zookeeperCliName + "得到节点对应的数据为:" + path + ":" + data);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + "获取数据失败：" + path + "---失败原因：" + e.getMessage());
        }
    }

    /**
     * 更新指定节点的内容
     *
     * @param zoo              zookeeper对象
     * @param path             节点的全路径
     * @param data             更新的数据
     * @param zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void setData(ZooKeeper zoo, String path, String data, String zookeeperCliName) {
        try {
            zoo.setData(path, data.getBytes(), -1); // -1忽略所有版本检查
            System.out.println(zookeeperCliName + ":节点更新成功  " + path + ":" + data);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + ":节点更新失败   " + path + ":" + data + "---失败原因：" + e.getMessage());
        }
    }

    /**
     * 删除节点
     *
     * @param zoo  ZooKeeper对象
     * @param path 节点路径
     * @param ,    String zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void deleteNode(ZooKeeper zoo, String path, String zookeeperCliName) {
        try {
            zoo.delete(path, -1);// -1忽略所有版本号
            System.out.println(zookeeperCliName + ":删除成功:" + path);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + ":删除节点失败：" + path + "--失败原因：" + e.getMessage());
        }
    }

    /**
     * @param zoo              ZooKeeper对象
     * @param path             节点路径
     * @param zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void exist(ZooKeeper zoo, String path, String zookeeperCliName) {
        try {
            zoo.exists(path, false);
            System.out.println(zookeeperCliName + ":判断节点是否存在成功:" + path);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + ":判断节点是否存在失败:" + path + "---失败原因:" + e.getMessage());
        }
    }

    /**
     * 创建一个具有认证权限的节点
     *
     * @param zoo              ZooKeeper对象
     * @param path             节点路径
     * @param data             节点数据
     * @param acls             权限
     * @param createMode       节点存储数据的模式
     * @param zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void createAuthNode(ZooKeeper zoo, String path, String data,
                                      List<ACL> acls, CreateMode createMode, String zookeeperCliName) {
        try {
            zoo.create(path, data.getBytes(), acls, createMode);
            System.out.println(zookeeperCliName + ":创建了一个具有相应权限的节点成功:" + path + ":" + data);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + ":创建了一个具有相应权限的节点失败:" + path + ":" + data + "---失败原因:" + e.getMessage());
        }
    }

    /**
     * 创建一个普通的节点（不具有权限约束的节点）
     *
     * @param zoo              ZooKeeper对象
     * @param path             节点路径
     * @param data             节点数据
     * @param zookeeperCliName 用来区别是哪一个zookeeper类调用的Util方法
     */
    public static void createNodePath(ZooKeeper zoo, String path, String data, String zookeeperCliName) {
        try {
            zoo.create(path, data.getBytes(), Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            System.out.println(zookeeperCliName + ":创建了一个新的节点成功:" + path + ":" + data);
        } catch (Exception e) {
            System.out.println(zookeeperCliName + ":创建了一个新的节点失败:" + path + ":" + data + "---失败原因:" + e.getMessage());
        }
    }

    /**
     * 释放资源
     *
     * @param zoo ZooKeeper对象
     */
    public static void close(ZooKeeper zoo) {
        if (zoo != null) {
            try {
                zoo.close();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
