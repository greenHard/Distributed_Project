package com.zhang.zookeeper.sence.dataSubPub;

import com.alibaba.fastjson.JSON;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;

import java.util.List;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.dataSubPub.ManageServer
 * @Description: 管理服务
 * @create 2018/09/26 15:36
 */
public class ManageServer {
    // zookeeper的servers节点路径
    private String serversPath;

    // zookeeper的command节点路径
    private String commandPath;

    // zookeeper的config节点路径
    private String configPath;

    private ZkClient zkClient;

    private ServerConfig config;

    // 用于监听servers节点的子节点列表的变化
    private IZkChildListener childListener;

    // 用于监听command节点数据内容的变化
    private IZkDataListener dataListener;

    // 工作服务器的列表
    private List<String> workServerList;

    public ManageServer(String serversPath, String commandPath,
                        String configPath, ZkClient zkClient, ServerConfig config) {
        this.serversPath = serversPath;
        this.commandPath = commandPath;
        this.zkClient = zkClient;
        this.config = config;
        this.configPath = configPath;
        this.childListener = (parentPath, currentChilds) -> {
            // 更新内存中工作服务器列表
            workServerList = currentChilds;
            System.out.println("work server list changed, new list is ");
            execList();
        };

        this.dataListener = new IZkDataListener() {

            public void handleDataDeleted(String dataPath) throws Exception {
                // ignore;
            }

            public void handleDataChange(String dataPath, Object data)
                    throws Exception {
                String cmd = new String((byte[]) data);
                System.out.println("cmd:" + cmd);
                // 执行命令
                exeCmd(cmd);

            }
        };
    }

    // 启动工作服务器
    public void start() {
        initRunning();
    }

    // 初始化
    private void initRunning() {
        zkClient.subscribeDataChanges(commandPath, dataListener);
        zkClient.subscribeChildChanges(serversPath, childListener);
    }

    // 停止工作服务器
    public void stop() {
        zkClient.unsubscribeChildChanges(serversPath, childListener);
        zkClient.unsubscribeDataChanges(commandPath, dataListener);
    }


    /*
     * 1: list 2: create 3: modify
     */
    private void exeCmd(String cmdType) {
        if ("list".equals(cmdType)) {
            execList();
        } else if ("create".equals(cmdType)) {
            execCreate();
        } else if ("modify".equals(cmdType)) {
            execModify();
        } else {
            System.out.println("error command!" + cmdType);
        }

    }

    // 列出工作服务器列表
    private void execList() {
        System.out.println(workServerList.toString());
    }

    // 创建config节点
    private void execCreate() {
        if (!zkClient.exists(configPath)) {
            try {
                zkClient.createPersistent(configPath, JSON.toJSONString(config)
                        .getBytes());
            } catch (ZkNodeExistsException e) {
                // config节点已经存在，则写入内容就可以了
                zkClient.writeData(configPath, JSON.toJSONString(config)
                        .getBytes());
            } catch (ZkNoNodeException e) {
                String parentDir = configPath.substring(0,
                        configPath.lastIndexOf('/'));
                zkClient.createPersistent(parentDir, true);
                execCreate();
            }
        }
    }

    // 修改config节点内容
    private void execModify() {
        // 我们随意修改config的一个属性就可以了
        config.setDbUser(config.getDbUser() + "_modify");
        try {
            zkClient.writeData(configPath, JSON.toJSONString(config).getBytes());
        } catch (ZkNoNodeException e) {
            // 写入时config节点还未存在，则创建它
            execCreate();
        }
    }

}
