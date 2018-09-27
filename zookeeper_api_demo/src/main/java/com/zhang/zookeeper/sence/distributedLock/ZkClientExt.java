package com.zhang.zookeeper.sence.distributedLock;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.ZkSerializer;
import org.apache.zookeeper.data.Stat;

/**
 * zk客户端 扩展
 */
public class ZkClientExt extends ZkClient {

    public ZkClientExt(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer) {
        super(zkServers, sessionTimeout, connectionTimeout, zkSerializer);
    }

    @Override
    public void watchForData(final String path) {
        retryUntilConnected(() -> {
            Stat stat = new Stat();
            // 对path进行监控
            _connection.readData(path, stat, true);
            return null;
        });
    }
}
