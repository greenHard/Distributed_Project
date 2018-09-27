package com.zhang.zookeeper.sence.distributedLock;

import java.util.concurrent.TimeUnit;

/**
 * 分布式锁
 */
public interface DistributedLock {

    /**
     *  获取锁
     */
    void acquire() throws Exception;


    /**
     * 获取锁直到超时
     */
    boolean acquire(long time, TimeUnit unit) throws Exception;

    /*
     * 释放锁
     */
    void release() throws Exception;


}
