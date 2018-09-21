package com.zhang.zookeeper.javaApi.acl;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 *
 * @author adai
 * @since 20170912 10:46
 * 测试zookeeper权限类
 */
public class TestAclDemo {
    private static ExecutorService executorService = null;
    private static ZooKeeper correctAuthZookeeper = null ;
    private static ZooKeeper badAuthZookeeper = null ;
    private static ZooKeeper notAuthZookeeper = null ;
    private static AuthZookeeper authZookeeper = null ;
    private static CountDownLatch countDownLatch = new CountDownLatch(4);
    public static void main(String[] args) throws InterruptedException {
//		executorService =  Executors.newFixedThreadPool(4);
        executorService =  new ThreadPoolExecutor(
                4, //  corePoolSize       核心线程数 （核心线程数不能大于最大线程数）
                4, //  maximumPoolSize    最大线程数 当使用无界队列时，该参数没有任何意义，无界队列执行的一次为核心线程的任务数
                2000, //  keepAliveTime 空闲时间
                TimeUnit.MILLISECONDS, // 时间单位(分)
                new LinkedBlockingQueue<Runnable>() // workQueue  使用何种队列机制
        );
        connect();
        countDownLatch.await(); // 等待所有zookeeper客户端连接服务器成功
        // 该客户端创建的节点具有权限，其他客户端要访问该节点，必须要与该节点的权限一致才能访问
        authZookeeper.createAuthNode(ZookeeperUtil.ROOT_PATH, " adai");

		exist();
        setData();
		getData();
		createChildNodePath();
        setChildNodeData();
		getChildNodeData();
		delete();
		createNodePathOrAllAuth();
        deleteNodeAllAuth();
        deleteRoot();
        close();
    }

    /**
     * 所有客户端异步连接zookeeper服务器
     */
    private static void connect(){
        executorService.execute(() -> {
            authZookeeper = new AuthZookeeper();
            countDownLatch.countDown();
        });

        executorService.execute(() -> {
            correctAuthZookeeper = new CorrectAuthZookeeper().getCorrectAuthZookeeper();
            countDownLatch.countDown();
        });

        executorService.execute(() -> {
            badAuthZookeeper = new BadAuthZookeeper().getCorrectAuthZookeeper();
            countDownLatch.countDown();
        });

        executorService.execute(() -> {
            notAuthZookeeper = new NotAuthZookeeper().getCorrectAuthZookeeper();
            countDownLatch.countDown();
        });
    }


    /**
     * AuthZookeeper:创建了一个具有相应权限的节点成功:/testAuth: adai
     * correctAuthZookeeper:判断节点是否存在成功:/testAuth
     * badAuthZookeeper:判断节点是否存在成功:/testAuth
     * notAuthZookeeper:判断节点是否存在成功:/testAuth
     */
    private static void exist(){
        ZookeeperUtil.exist(correctAuthZookeeper, ZookeeperUtil.ROOT_PATH, "correctAuthZookeeper");
        ZookeeperUtil.exist(badAuthZookeeper, ZookeeperUtil.ROOT_PATH, "badAuthZookeeper");
        ZookeeperUtil.exist(notAuthZookeeper, ZookeeperUtil.ROOT_PATH, "notAuthZookeeper");
    }


    /**
     * correctAuthZookeeper:节点更新成功  /testAuth:auth correctAuthZookeeper
     * badAuthZookeeper:节点更新失败      /testAuth:auth badAuthZookeeper---失败原因：KeeperErrorCode = NoAuth for /testAuth
     * notAuthZookeeper:节点更新失败   /testAuth:auth notAuthZookeeper---失败原因：KeeperErrorCode = NoAuth for /testAuth
     */
    private static void setData(){
        ZookeeperUtil.setData(correctAuthZookeeper,ZookeeperUtil.ROOT_PATH,
                "auth correctAuthZookeeper","correctAuthZookeeper");
        ZookeeperUtil.setData(badAuthZookeeper,ZookeeperUtil.ROOT_PATH,
                "auth badAuthZookeeper","badAuthZookeeper");
        ZookeeperUtil.setData(notAuthZookeeper,ZookeeperUtil.ROOT_PATH,
                "auth notAuthZookeeper","notAuthZookeeper");
    }


    /**
     * correctAuthZookeeper得到节点对应的数据为:/testAuth: adai
     * badAuthZookeeper获取数据失败：/testAuth---失败原因：KeeperErrorCode = NoAuth for /testAuth
     * notAuthZookeeper获取数据失败：/testAuth---失败原因：KeeperErrorCode = NoAuth for /testAuth
     */
    private static void getData(){
        ZookeeperUtil.getData(correctAuthZookeeper, ZookeeperUtil.ROOT_PATH, "correctAuthZookeeper");
        ZookeeperUtil.getData(badAuthZookeeper, ZookeeperUtil.ROOT_PATH, "badAuthZookeeper");
        ZookeeperUtil.getData(notAuthZookeeper, ZookeeperUtil.ROOT_PATH, "notAuthZookeeper");
    }


    /**
     *correctAuthZookeeper:创建了一个新的节点成功:/testAuth/children: adai children correctAuthZookeeper
     *badAuthZookeeper:创建了一个新的节点失败:/testAuth/children:
     *	adai children badAuthZookeeper---失败原因:KeeperErrorCode = NoAuth for /testAuth/children
     *notAuthZookeeper:创建了一个新的节点失败:/testAuth/children:
     *	adai children notAuthZookeeper---失败原因:KeeperErrorCode = NoAuth for /testAuth/children
     */
    private static void createChildNodePath(){
        ZookeeperUtil.createNodePath(correctAuthZookeeper, ZookeeperUtil.CHILDREN,
                " adai children correctAuthZookeeper", "correctAuthZookeeper");
        ZookeeperUtil.createNodePath(badAuthZookeeper, ZookeeperUtil.CHILDREN+"2",
                " adai children badAuthZookeeper", "badAuthZookeeper");
        ZookeeperUtil.createNodePath(notAuthZookeeper, ZookeeperUtil.CHILDREN+"3",
                " adai children notAuthZookeeper", "notAuthZookeeper");
    }

    /**
     * correctAuthZookeeper:节点更新成功  /testAuth/children:set nodeData correctAuthZookeeper
     * badAuthZookeeper:节点更新成功  /testAuth/children:set nodeData badAuthZookeeper
     * notAuthZookeeper:节点更新成功  /testAuth/children:set nodeData notAuthZookeeper
     * 疑问?为什么子节点会更新成功(请比较两个创建自己的方法有什么不同)
     */
    private static void setChildNodeData(){
        ZookeeperUtil.setData(correctAuthZookeeper, ZookeeperUtil.CHILDREN,
                "set nodeData correctAuthZookeeper", "correctAuthZookeeper");
        ZookeeperUtil.setData(badAuthZookeeper, ZookeeperUtil.CHILDREN,
                "set nodeData badAuthZookeeper", "badAuthZookeeper");
        ZookeeperUtil.setData(notAuthZookeeper, ZookeeperUtil.CHILDREN,
                "set nodeData notAuthZookeeper", "notAuthZookeeper");
    }

    /**
     * correctAuthZookeeper得到节点对应的数据为:/testAuth/children:set nodeData notAuthZookeeper
     * badAuthZookeeper得到节点对应的数据为:/testAuth/children:set nodeData notAuthZookeeper
     * notAuthZookeeper得到节点对应的数据为:/testAuth/children:set nodeData notAuthZookeeper
     * 答上疑问，因为correctAuthZookeeper创建子节点没有将所有权限作为参数传入，而用的是Ids.OPEN_ACL_UNSAFE传入。
     */
    private static void getChildNodeData(){
        ZookeeperUtil.getData(correctAuthZookeeper, ZookeeperUtil.CHILDREN, "correctAuthZookeeper");
        ZookeeperUtil.getData(badAuthZookeeper, ZookeeperUtil.CHILDREN, "badAuthZookeeper");
        ZookeeperUtil.getData(notAuthZookeeper, ZookeeperUtil.CHILDREN, "notAuthZookeeper");
    }


    /**
     * badAuthZookeeper:删除节点失败：/testAuth/children--失败原因：KeeperErrorCode = NoAuth for /testAuth/children
     * notAuthZookeeper:删除节点失败：/testAuth/children--失败原因：KeeperErrorCode = NoAuth for /testAuth/children
     * correctAuthZookeeper:删除成功:/testAuth/children
     */
    public static void delete(){
        ZookeeperUtil.deleteNode(badAuthZookeeper, ZookeeperUtil.CHILDREN,"badAuthZookeeper");
        ZookeeperUtil.deleteNode(notAuthZookeeper, ZookeeperUtil.CHILDREN,"notAuthZookeeper");
        ZookeeperUtil.deleteNode(correctAuthZookeeper, ZookeeperUtil.CHILDREN,"correctAuthZookeeper");
    }

    /**
     * 解决上文疑问
     * CorrectAuthZookeeper:创建了一个具有相应权限的节点成功:/testAuth/children2: adai all atuh create correctAuthZookeeper
     * correctAuthZookeeper:节点更新成功  /testAuth/children2:set nodeData to AllAuth correctAuthZookeeper
     * badAuthZookeeper:节点更新失败   /testAuth/children2:set nodeData2 to AllAuth badAuthZookeeper
     * 		---失败原因：KeeperErrorCode = NoAuth for /testAuth/children2
     * notAuthZookeeper:节点更新失败   /testAuth/children2:set nodeData2 to AllAuth notAuthZookeeper
     * 		---失败原因：KeeperErrorCode = NoAuth for /testAuth/children2
     */
    public static void createNodePathOrAllAuth(){
        try {
            Stat stat = correctAuthZookeeper.exists(ZookeeperUtil.CHILDRENAUTH, false);
            if (stat != null){
                System.out.println(ZookeeperUtil.CHILDRENAUTH+"节点已经存在:");
                return ;
            }
        } catch (Exception e) {
        }
        List<ACL> acls = new ArrayList<ACL>(1);
        for (ACL ids_acl : Ids.CREATOR_ALL_ACL) { //遍历出所有权限类型
            acls.add(ids_acl);
        }
        ZookeeperUtil.createAuthNode(correctAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,
                " adai all atuh create correctAuthZookeeper", acls, CreateMode.PERSISTENT, "CorrectAuthZookeeper");
        ZookeeperUtil.setData(correctAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,
                "set nodeData to AllAuth correctAuthZookeeper", "correctAuthZookeeper");
        ZookeeperUtil.setData(badAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,
                "set nodeData2 to AllAuth badAuthZookeeper", "badAuthZookeeper");
        ZookeeperUtil.setData(notAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,
                "set nodeData2 to AllAuth notAuthZookeeper", "notAuthZookeeper");
    }

    public static void deleteNodeAllAuth(){
        ZookeeperUtil.deleteNode(badAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,"badAuthZookeeper");
        ZookeeperUtil.deleteNode(notAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,"notAuthZookeeper");
        ZookeeperUtil.deleteNode(correctAuthZookeeper, ZookeeperUtil.CHILDRENAUTH,"correctAuthZookeeper");
    }


    private static void deleteRoot(){
        ZookeeperUtil.deleteNode(badAuthZookeeper, ZookeeperUtil.ROOT_PATH,"badAuthZookeeper");
        ZookeeperUtil.deleteNode(notAuthZookeeper, ZookeeperUtil.ROOT_PATH,"notAuthZookeeper");
        ZookeeperUtil.deleteNode(correctAuthZookeeper, ZookeeperUtil.ROOT_PATH,"correctAuthZookeeper");
    }

    /**
     * 释放资源
     */
    private static void close(){
        ZookeeperUtil.close(correctAuthZookeeper);
        ZookeeperUtil.close(badAuthZookeeper);
        ZookeeperUtil.close(notAuthZookeeper);
        ZookeeperUtil.close(authZookeeper.getCorrectAuthZookeeper());
        executorService.shutdown(); // 等待任务执行完后释放资源
        System.out.println("释放资源完毕");
    }

}
