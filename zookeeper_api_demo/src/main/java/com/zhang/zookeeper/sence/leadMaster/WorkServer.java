package com.zhang.zookeeper.sence.leadMaster;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.leadMaster.WorkServer
 * @Description: 工作服务器
 * @create 2018/09/26 14:22
 */
public class WorkServer {

    // 记录服务器状态
    private volatile boolean running = false;

    private ZkClient zkClient;

    // Master节点对应zookeeper中的节点路径
    private static final String MASTER_PATH = "/master";

    // 监听Master节点删除事件
    private IZkDataListener dataListener;

    // 记录当前节点的基本信息
    private RunningData serverData;

    // 记录集群中Master节点的基本信息
    private RunningData masterData;

    private ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);

    private int delayTime = 5;

    public WorkServer(RunningData rd){
        this.serverData = rd;
        this.dataListener = new IZkDataListener() {
            @Override
            public void handleDataDeleted(String dataPath){
                if(masterData != null && masterData.getName().equals(serverData.getName())){
                    // 自己就是上一轮的Master服务器，则直接抢
                    takeMaster();
                } else {
                    // 否则，延迟5秒后再抢。主要是应对网络抖动，给上一轮的Master服务器优先抢占master的权利，避免不必要的数据迁移开销
                    delayExector.schedule(() -> takeMaster(), delayTime, TimeUnit.SECONDS);
                }
            }

            @Override
            public void handleDataChange(String dataPath, Object data){

            }
        };
    }

    // 启动服务器
    public void start() throws Exception{
        if(running){
            throw new Exception("server has startup...");
        }
        running = true;

        // 订阅Master节点删除事件
        zkClient.subscribeDataChanges(MASTER_PATH, dataListener);

        // 争抢Master权利
        takeMaster();
    }

    // 停止服务器
    public void stop() throws Exception{
        if(!running){
            throw new Exception("server has stoped...");
        }
        running = false;
        delayExector.shutdown();
        // 取消Master节点事件订阅
        zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
        // 释放Master权利
        releaseMaster();
    }

    // 释放master
    private void releaseMaster() {
        if (checkMaster()) {
            zkClient.delete(MASTER_PATH);
        }
    }


    // 检查节点是否是master
    private boolean checkMaster() {
        try {
            // 查看运行节点是否等于master节点
            masterData = zkClient.readData(MASTER_PATH);
            return masterData.getName().equals(serverData.getName());
        } catch (ZkNoNodeException e) {
            return false; // 节点不存在，自己肯定不是Master了
        } catch (ZkInterruptedException e) {
            return checkMaster();
        } catch (ZkException e) {
            return false;
        }
    }

    // 争抢Master
    private void takeMaster() {
        if (!running)
            return;
        try {
            // 尝试创建Master临时节点
            zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
            masterData = serverData;
            System.out.println(serverData.getName()+" is master");

            // 作为演示，我们让服务器每隔5秒释放一次Master权利
            delayExector.schedule(() -> {
                if (checkMaster()){
                    releaseMaster();
                }
            }, 5, TimeUnit.SECONDS);
        } catch (ZkNodeExistsException e) {
            // 已被其他服务器创建了,读取Master节点信息
            RunningData runningData = zkClient.readData(MASTER_PATH, true);
            if (runningData == null) {
                // 没读到，读取瞬间Master节点宕机了，有机会再次争抢
                takeMaster();
            } else {
                masterData = runningData;
            }
        } catch (Exception e) {
            // ignore;
        }

    }

    public ZkClient getZkClient() {
        return zkClient;
    }

    public void setZkClient(ZkClient zkClient) {
        this.zkClient = zkClient;
    }



}
