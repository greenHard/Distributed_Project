package com.zhang.zookeeper.sence.distributedLock;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * 基础分布式锁
 */
public class BaseDistributedLock {

    /**
     * zk
     */
    private final ZkClientExt client;

    /**
     * path 路径
     */
    private final String path;

    /**
     * basePath 基础路径
     */
    private final String basePath;

    /**
     * lock name 锁的名称
     */
    private final String lockName;

    /**
     * max retry times
     */
    private static final Integer MAX_RETRY_COUNT = 10;

    public BaseDistributedLock(ZkClientExt client, String basePath, String lockName) {
        this.client = client;
        this.basePath = basePath;
        this.path = basePath.concat("/").concat(lockName);
        this.lockName = lockName;
    }

    // 创建临时顺序节点
    private String createLockNode(ZkClient client, String path) throws Exception {
        return client.createEphemeralSequential(path, null);
    }

    // 删除成功获取锁之后所创建的那个顺序节点
    private void deleteOurPath(String ourPath) throws Exception {
        client.delete(ourPath);
    }

    // 释放锁
    public void releaseLock(String lockPath) throws Exception {
        deleteOurPath(lockPath);
    }


    // 获取/locker下的经过排序的子节点列表
    private List<String> getSortedChildren() {
        try {
            List<String> children = client.getChildren(basePath);
            children.sort(Comparator.comparing(lhs -> getLockNodeNumber(lhs, lockName)));
            return children;
        } catch (ZkNoNodeException e) {
            client.createPersistent(basePath, true);
            return getSortedChildren();
        }
    }

    // 获取锁节点名称
    private String getLockNodeNumber(String str, String lockName) {
        int index = str.lastIndexOf(lockName);
        if (index >= 0) {
            index += lockName.length();
            return index <= str.length() ? str.substring(index) : "";
        }
        return str;
    }


    /**
     * 尝试获取锁
     * 1. 判断开始时间，需要等待时间
     * 2. 是否需要重试
     * 3. 如果网络出现异常,重试
     * 4. 创建临时节点
     * 5. 去等待获取锁 成功、失败
     */
    protected String attemptLock(long time, TimeUnit unit) throws Exception {
        // 开始时间
        final long startMillis = System.currentTimeMillis();
        // 需要等待时间
        final Long millisToWait = (unit != null) ? unit.toMillis(time) : null;
        String ourPath = null;
        // 是否需要重试
        boolean isDone = false;
        // 重试次数
        int retryCount = 0;
        //网络闪断需要重试一试
        while (!isDone) {
            isDone = true;
            try {
                // 在/locker下创建临时的顺序节点
                ourPath = createLockNode(client, path);
                // 判断自己是否获得了锁，如果没有获得那么等待直到获得锁或者超时
                boolean haveTheLock = waitToLock(startMillis, millisToWait, ourPath);
                System.out.println("获取锁的状态: " + haveTheLock);
            } catch (ZkNoNodeException e) { // 捕获这个异常
                if (retryCount++ < MAX_RETRY_COUNT) { // 重试指定次数
                    isDone = false;
                } else {
                    throw e;
                }
            }
        }
        return ourPath;
    }

    /**
     * 1. 判断是否拥有锁,
     * 2. 存在直接返回true,如果不存在锁
     * 3. 取节点下面的所有子节点，进行排序
     * 4. 获取自己刚刚创建的节点名,判断在集合中的位置
     * 5. 如果是第一个,代表自己已经获得了锁
     * 6. 如果不是第一个，对自己的前一个节点进行watch,进行订阅
     * 7. 如果设有超时时间,等待超时时间,如果没有获取到返回false,获取到了返回true
     * 8. 最后在获取时间中，如果出现异常和获取失败,删除节点
     */
    private boolean waitToLock(long startMillis, Long millisToWait, String ourPath) throws Exception {
        // 是否拥有锁
        boolean haveTheLock = false;
        // 确认删除
        boolean doDelete = false;
        try {
            while (!haveTheLock) {
                // 获取/locker下的经过排序的子节点列表
                List<String> children = getSortedChildren();

                // 获取刚才自己创建的那个顺序节点名
                String sequenceNodeName = ourPath.substring(basePath.length() + 1);

                // 判断自己排第几个
                int ourIndex = children.indexOf(sequenceNodeName);
                if (ourIndex < 0) {
                    // 网络抖动，获取到的子节点列表里可能已经没有自己了
                    throw new ZkNoNodeException("节点没有找到: " + sequenceNodeName);
                }

                // 如果是第一个，代表自己已经获得了锁
                boolean isGetTheLock = ourIndex == 0;

                // 如果自己没有获得锁，则要watch比我们次小的那个节点
                String pathToWatch = isGetTheLock ? null : children.get(ourIndex - 1);

                if (isGetTheLock) {
                    haveTheLock = true;
                } else {
                    // 订阅比自己小顺序节点的删除事件
                    String previousSequencePath = basePath.concat("/").concat(pathToWatch);
                    final CountDownLatch latch = new CountDownLatch(1);
                    final IZkDataListener previousListener = new IZkDataListener() {
                        public void handleDataDeleted(String dataPath) throws Exception {
                            // 删除后结束latch上的await
                            latch.countDown();
                        }

                        public void handleDataChange(String dataPath, Object data) throws Exception {
                            // ignore
                        }
                    };

                    try {
                        // 订阅次小顺序节点的删除事件，如果节点不存在会出现异常
                        client.subscribeDataChanges(previousSequencePath, previousListener);
                        if (millisToWait != null) {
                            // 等待时间
                            millisToWait -= (System.currentTimeMillis() - startMillis);
                            // 开始时间
                            startMillis = System.currentTimeMillis();
                            if (millisToWait <= 0) {
                                // timed out - delete our node
                                doDelete = true;
                                break;
                            }
                            // 在latch上await
                            latch.await(millisToWait, TimeUnit.MICROSECONDS);
                        } else {
                            // 在latch上await
                            latch.await();
                        }
                        // 结束latch上的等待后，继续while重新来过判断自己是否第一个顺序节点
                    } catch (ZkNoNodeException e) {
                        //ignore
                    } finally {
                        client.unsubscribeDataChanges(previousSequencePath, previousListener);
                    }

                }
            }
        } catch (Exception e) {
            //发生异常需要删除节点
            doDelete = true;
            throw e;
        } finally {
            //如果需要删除节点
            if (doDelete) {
                deleteOurPath(ourPath);
            }
        }
        return haveTheLock;
    }


}
