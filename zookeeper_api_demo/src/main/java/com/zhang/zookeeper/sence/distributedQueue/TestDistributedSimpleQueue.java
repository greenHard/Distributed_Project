package com.zhang.zookeeper.sence.distributedQueue;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.distributedQueue.TestDistributedSimpleQueue
 * @Description: 测试分布式队列
 * @create 2018/09/29 10:14
 */
public class TestDistributedSimpleQueue {
    public static void main(String[] args) {

        ZkClient zkClient = new ZkClient("10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181", 5000, 5000, new SerializableSerializer());

        DistributedSimpleQueue<User> queue = new DistributedSimpleQueue<>(zkClient,"/Queue");

        User user1 = new User();
        user1.setId("1");
        user1.setName("bill");

        User user2 = new User();
        user2.setId("2");
        user2.setName("Alex");

        try {
            // 先进先出
            queue.offer(user1);
            queue.offer(user2);
            User u1 = queue.poll();
            User u2 = queue.poll();
            if (user1.getId().equals(u1.getId()) && user2.getId().equals(u2.getId())){
                System.out.println("Success!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
