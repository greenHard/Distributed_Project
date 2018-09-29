package com.zhang.zookeeper.sence.distributedQueue;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.distributedQueue.TestDistributeBlockingQueue
 * @Description: 测试分布式阻塞队列
 * @create 2018/09/29 10:22
 */
public class TestDistributeBlockingQueue {

    public static void main(String[] args) {

        ScheduledExecutorService delayExecutor = Executors.newScheduledThreadPool(1);
        int delayTime = 5;

        ZkClient zkClient = new ZkClient("10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181", 5000, 5000, new SerializableSerializer());

        DistributedBlockingQueue<User> queue = new DistributedBlockingQueue<>(zkClient, "/Queue");


        final User user1 = new User();
        user1.setId("1");
        user1.setName("alex");

        final User user2 = new User();
        user2.setId("2");
        user2.setName("bill");

        try {
            delayExecutor.schedule(() -> {
                try {
                    queue.offer(user1);
                    queue.offer(user2);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }, delayTime, TimeUnit.SECONDS);

            System.out.println("ready poll!");
            User u1 = queue.poll();
            User u2 = queue.poll();

            if (user1.getId().equals(u1.getId()) && user2.getId().equals(u2.getId())) {
                System.out.println("Success!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            delayExecutor.shutdown();
            try {
                delayExecutor.awaitTermination(2, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                // ignore
            }
        }
    }
}
