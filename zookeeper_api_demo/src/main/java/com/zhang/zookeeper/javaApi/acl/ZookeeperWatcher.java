package com.zhang.zookeeper.javaApi.acl;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event.EventType;
import org.apache.zookeeper.Watcher.Event.KeeperState;

import java.util.concurrent.CountDownLatch;
/**
 *
 * @author adai
 * @since 20170913 0132
 */

/**
 * ZookeeperWatcher类,如果使用了watcher事件会触发该类的process方法。
 * 第一次连接zookeeper服务器的时候也会触发
 * @author Administrator
 *
 */
public class ZookeeperWatcher implements Watcher{
    //用来阻塞线程，等待客户端成功连接zookeeper服务器。
    private   CountDownLatch correctAuthZooKeeper = null;

    //用来打印日记时，目的是为了知道是哪一个创建的zookeeper
    private String log_process = "";

    public 	ZookeeperWatcher(CountDownLatch correctAuthZooKeeper , String zookeeperCliName){
        this.correctAuthZooKeeper = correctAuthZooKeeper;
        log_process = zookeeperCliName;
    }

    @Override
    public void process(WatchedEvent event) {
        if(event == null ) return;
        log_process = "【"+log_process+" watche-"+ZookeeperUtil.SEQ.addAndGet(1)+"】";
        String path = event.getPath(); // 受影响的节点路径
        EventType enventType = event.getType(); // 事件类型(一共有四种)
        KeeperState stat = event.getState() ; // ZK状态(一共有四种)

        if(KeeperState.SyncConnected == stat){
            if(EventType.None == enventType){
                System.out.println(log_process+":连接zookeeper服务器成功");
                correctAuthZooKeeper.countDown();
            }
        }else if(KeeperState.Disconnected == stat){
            System.out.println(log_process+":连接zookeeper服务器失败");
        }else if(KeeperState.Expired == stat){
            System.out.println(log_process+":连接zookeeper服务器会话已经过期");
        }else if(KeeperState.AuthFailed == stat){
            System.out.println(log_process+":连接zookeeper服务器认证失败");
        }
        System.out.println("------------------------------------");
    }
}
