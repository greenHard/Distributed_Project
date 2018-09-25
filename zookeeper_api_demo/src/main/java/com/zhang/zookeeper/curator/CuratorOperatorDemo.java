package com.zhang.zookeeper.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.transaction.CuratorTransactionResult;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;

import java.util.Collection;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.CuratorOperatorDemo
 * @Description: curator API操作 增删改查
 * @create 2018/09/25 13:08
 */
public class CuratorOperatorDemo {

    public static void main(String[] args) throws Exception {
        CuratorFramework curatorFramework = CuratorClientUtils.getInstance();
        System.out.println("连接成功.........");

        // fluent风格
        // 创建节点
        String result = curatorFramework.create() // 创建一个createBuilder
                .creatingParentsIfNeeded() // 创建父节点如果需要
                .withMode(CreateMode.PERSISTENT) // 永久节点
                .forPath("/curator/curator1/curator11", "123".getBytes());// 创建路径和数据

        System.out.println("创建节点成功,result:  " + result);

        // 删除节点
        curatorFramework.delete()  // 创建一个deleteBuilder
                .deletingChildrenIfNeeded() // 删除子节点如果需要的话
                .forPath("/curator/curator1/curator11");// 删除路径
        System.out.println("删除节点成功...");


        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath("/curator1", "234".getBytes());

        // 查询
        Stat stat = new Stat();
        byte[] bytes = curatorFramework.getData() // 创建一个getBuilder
                .storingStatIn(stat)    // 存储
                .forPath("/curator1");  // 路径
        System.out.println(new String(bytes) + "-->stat:" + stat);

        // 更新
        Stat stat1 = curatorFramework.setData() // 创建setBuilder
                .forPath("/curator", "456".getBytes());// 路径和值
        System.out.println("成功之后的stat: " + stat1);


        // 异步操作
        CountDownLatch countDownLatch = new CountDownLatch(1);
        ExecutorService executorService = Executors.newFixedThreadPool(1);
        curatorFramework.create()
                .creatingParentsIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .inBackground((client, event) -> {
                    System.out.println(Thread.currentThread().getName() + "->resultCode:" + event.getResultCode() + "->"
                            + event.getType());
                    countDownLatch.countDown();
                }, executorService)
                .forPath("/bill", "234".getBytes());

        countDownLatch.await();
        // 关闭线程池
        executorService.shutdown();


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
