package com.zhang.zookeeper.sence.dataSubPub;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.dataSubPub.WorkServer
 * @Description: 工作服务器
 * @create 2018/09/26 15:21
 */
public class WorkServer {

    // zk
    private ZkClient zkClient;

    // ZooKeeper
    private String configPath;

    // ZooKeeper集群中servers节点的路径
    private String serversPath;

    // 当前工作服务器的基本信息
    private ServerData serverData;

    // 当前工作服务器的配置信息
    private ServerConfig serverConfig;

    private IZkDataListener dataListener;

    public WorkServer(String configPath, String serversPath,
                      ServerData serverData, ZkClient zkClient, ServerConfig initConfig) {
        this.zkClient = zkClient;
        this.serversPath = serversPath;
        this.configPath = configPath;
        this.serverConfig = initConfig;
        this.serverData = serverData;
        this.dataListener = new IZkDataListener() {
            public void handleDataChange(String dataPath, Object data) {
                // 改变的数据
                String retJson = new String((byte[])data);
                // 将json转换成对象
                ServerConfig serverConfigLocal = JSON.parseObject(retJson,ServerConfig.class);
                // 更新工作服务器的配置信息
                updateConfig(serverConfigLocal);
                System.out.println("new Work server config is:"+serverConfig.toString());

            }

            public void handleDataDeleted(String dataPath){
                // ignore;
            }
        };
    }

    // 启动服务器
    public void start() {
        System.out.println("work server start...");
        initRunning();
    }

    // 服务器初始化
    private void initRunning() {
        // 注册自己
        registMe();
        // 订阅config节点的改变事件
        zkClient.subscribeDataChanges(configPath, dataListener);
    }

    // 启动时向zookeeper注册自己的注册函数
    private void registMe() {
        String mePath = serversPath.concat("/").concat(serverData.getAddress());
        try {
            zkClient.createEphemeral(mePath, JSON.toJSONString(serverData)
                    .getBytes());
        } catch (ZkNoNodeException e) {
            // 如果节点不存在,先创建parent永久节点
            zkClient.createPersistent(serversPath, true);
            registMe();
        }
    }

    // 停止服务器
    public void stop() {
        System.out.println("work server stop...");
        // 取消监听config节点
        zkClient.unsubscribeDataChanges(configPath, dataListener);
    }

    // 更新自己的配置信息
    private void updateConfig(ServerConfig serverConfig) {
        this.serverConfig = serverConfig;
    }

}
