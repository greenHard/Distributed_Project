package com.zhang.zookeeper.sence.distributedQueue;

import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.concurrent.CountDownLatch;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.distributedQueue.DistributedBlockingQueue
 * @Description: 阻塞式分布式队列
 * @create 2018/09/29 10:18
 */
public class DistributedBlockingQueue<T> extends DistributedSimpleQueue<T>{

    DistributedBlockingQueue(ZkClient zkClient, String root) {
        super(zkClient, root);
    }


    @Override
    public T poll() throws Exception {

        while (true){
            // 结束在latch上的等待后，再来一次
            final CountDownLatch latch = new CountDownLatch(1);

            final IZkChildListener childListener = (parentPath, currentChilds) -> {
                // 队列有变化，结束latch上的等待
                latch.countDown();
            };

            zkClient.subscribeChildChanges(root, childListener);

            try{
                // 获取队列数据
                T node = super.poll();
                if ( node != null ){
                    return node;
                } else {
                    // 拿不到队列数据，则在latch上await
                    latch.await();
                }
            } finally {
                zkClient.unsubscribeChildChanges(root, childListener);
            }
        }
    }

}
