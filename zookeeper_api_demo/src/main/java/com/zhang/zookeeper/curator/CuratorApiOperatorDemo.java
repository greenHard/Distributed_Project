package com.zhang.zookeeper.curator;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.CuratorApiOperatorDemo
 * @Description: curator 操作使用
 * @create 2018/09/26 13:16
 */
public class CuratorApiOperatorDemo {

    /**
     * 集群连接地址
     */
    // public static final String CONNECT_STRING = "192.168.0.101:2181,192.168.0.104:2181,192.168.0.105:2181";
    private static final String CONNECT_STRING = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";

    /**
     * curator 客户端
     */
    private CuratorFramework curatorFramework;

    /**
     * 重试策略
     */
    private RetryPolicy retryPolicy = new ExponentialBackoffRetry(2000, 3);

    @Before
    public void init() {
        curatorFramework = CuratorFrameworkFactory.builder()
                .sessionTimeoutMs(2000)
                .connectionTimeoutMs(2000)
                .connectString(CONNECT_STRING)
                .retryPolicy(retryPolicy)
                .build();

        curatorFramework.start();
    }

    /**
     * 创建节点
     */
    @Test
    public void createNode() throws Exception {
        String path = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .forPath("/curator/test", "hello".getBytes());
        System.out.println("创建成功,路径: " + path);
    }

    /**
     * 获取节点内容
     */
    @Test
    public void getData() throws Exception {
        Stat stat = new Stat();
        byte[] bytes = curatorFramework.getData()
                .storingStatIn(stat)
                .forPath("/curator/test");
        System.out.println("获取内容为data: " + new String(bytes));
        System.out.println("获取内容stat: " + stat);
    }


    /**
     * 获取子节点
     */
    @Test
    public void getChild() throws Exception {
        List<String> child = curatorFramework.getChildren()
                .forPath("/curator");
        System.out.println("子节点列表为: " + child);
    }

    /**
     * 检测节点
     */
    @Test
    public void nodeExists() throws Exception {
        ExecutorService es = Executors.newFixedThreadPool(5);

        // curatorFramework.checkExists().forPath("/curator");

        // 异步监控某个节点
        curatorFramework.checkExists()
                .inBackground((client, event) -> {
                    Stat stat = event.getStat();
                    System.out.println("/curator 的stat: " + stat);
                    System.out.println("/curator event的内容:" + event.getContext());
                }, "123", es).forPath("/curator");

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    /**
     * 修改节点数据
     */
    @Test
    public void writeNode() throws Exception {
        Stat stat = new Stat();
        curatorFramework.getData().storingStatIn(stat).forPath("/curator");
        curatorFramework.setData().withVersion(stat.getVersion()).forPath("/curator", "234".getBytes());
    }

    /**
     * 节点监听
     */
    @Test
    public void nodeListener() throws Exception {
        final NodeCache cache = new NodeCache(curatorFramework, "/curator");
        cache.start();
        cache.getListenable().addListener(() -> {
            byte[] data = cache.getCurrentData().getData();
            System.out.println("数据改变了,data: " + new String(data));
        });
        curatorFramework.setData().forPath("/curator", "345".getBytes());
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    /**
     * 子节点监听
     */
    @Test
    public void nodeChildrenListener() throws Exception {
        final PathChildrenCache cache = new PathChildrenCache(curatorFramework, "/curator", true);
        cache.start();
        cache.getListenable().addListener((client, event) -> {
            switch (event.getType()) {
                case CHILD_UPDATED:
                    System.out.println("CHILD_UPDATED:" + event.getData());
                    break;
                case CHILD_REMOVED:
                    System.out.println("CHILD_REMOVED:" + event.getData());
                    break;
                case CHILD_ADDED:
                    System.out.println("CHILD_ADDED:" + event.getData());
                    break;
                default:
                    break;
            }
        });


        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/curator/child", "123".getBytes());
        TimeUnit.SECONDS.sleep(1);

        curatorFramework.setData()
                .forPath("/curator/child", "456".getBytes());
        TimeUnit.SECONDS.sleep(1);

        curatorFramework.delete()
                .forPath("/curator/child");
        TimeUnit.SECONDS.sleep(1);

        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
    }

    /**
     * 创建节点权限
     */
    @Test
    public void createNodeAuth() throws Exception {
        ACL aclIp = new ACL(ZooDefs.Perms.READ, new Id("ip", "192.168.1.105"));
        ACL aclDigest = new ACL(ZooDefs.Perms.READ | ZooDefs.Perms.WRITE, new Id("digest", DigestAuthenticationProvider.generateDigest("bill:123456")));
        ArrayList<ACL> acl = new ArrayList<>();
        acl.add(aclIp);
        acl.add(aclDigest);

        String path = curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.PERSISTENT)
                .withACL(acl)
                .forPath("/curator/auth", "3".getBytes());
        System.out.println(path);

    }


    /**
     * 事务操作,curator 独有的
     */
    @Test
    public void tran() throws Exception {
        // 事务操作(curator 独有的)
        Collection<CuratorTransactionResult> results = curatorFramework.inTransaction()
                .create()
                .forPath("/trans", "111".getBytes())
                .and()
                .setData()
                .forPath("/trans", "222".getBytes())
                .and()
                .commit();
        for (CuratorTransactionResult curatorTransactionResult : results) {
            System.out.println(curatorTransactionResult.getForPath() + "->" + curatorTransactionResult.getType());
        }

    }
}
