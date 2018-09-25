package com.zhang.zookeeper.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.curator.CuratorEventDemo
 * @Description: curator事件Demo
 * Cache是Curator对事件监听的包装，其对事件的监听可以近似看做是一个本地缓存视图和远程ZooKeeper视图的对比过程。
 * 而且Curator会自动的再次监听，我们就不需要自己手动的重复监听了。
 * @create 2018/09/25 13:47
 */
public class CuratorEventDemo {
    public static void main(String[] args) throws Exception {
        // 创建实例
        CuratorFramework curatorFramework = CuratorClientUtils.getInstance();

        // NodeCache是用来监听节点的数据变化的，当监听的节点的数据发生变化的时候就会回调对应的函数.
        NodeCache cache = new NodeCache(curatorFramework, "/curator", false);
        cache.start(true);
        cache.getListenable().addListener(() ->
                System.out.println("节点数据发生改变了,变化之后的数据为: " + new String(cache.getCurrentData().getData()))
        );
        System.out.println("=====================");
        curatorFramework.setData().forPath("/curator", "zhang".getBytes());


        // PathChildrenCache是用来监听指定节点 的子节点变化情况。共有六种构造方法（有两种弃用了，否则就是八种）
        PathChildrenCache pathChildrenCache = new PathChildrenCache(curatorFramework, "/event", true);
        pathChildrenCache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        // 添加监听事件
        pathChildrenCache.getListenable().addListener((client, event) -> {
            // 事件类型
            PathChildrenCacheEvent.Type type = event.getType();

            if (type == PathChildrenCacheEvent.Type.INITIALIZED) {
                System.out.println("子节点初始化！");
            }

            System.out.println("pathChildrenCache------发生的节点变化类型为：" + event.getType() + ",发生变化的节点内容为：" + new String(event.getData().getData()) + ",路径：" + event.getData().getPath());

            switch (type) {
                case CHILD_ADDED:
                    System.out.println("增加子节点");
                    break;
                case CHILD_REMOVED:
                    System.out.println("删除子节点");
                    break;
                case CHILD_UPDATED:
                    System.out.println("更新子节点");
                    break;
                default:
                    break;
            }
        });

        curatorFramework.create().withMode(CreateMode.PERSISTENT).forPath("/event","event".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("1");

        // 创建子节点
        curatorFramework.create().withMode(CreateMode.EPHEMERAL).forPath("/event/event1","1".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("2");

        // 修改子节点数据
        curatorFramework.setData().forPath("/event/event1","222".getBytes());
        TimeUnit.SECONDS.sleep(1);
        System.out.println("3");

        // 删除子节点
        curatorFramework.delete().forPath("/event/event1");
        System.out.println("4");

        // 保持程序运行,等待回调
        System.in.read();

    }
}
