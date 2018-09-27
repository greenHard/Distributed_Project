package com.zhang.zookeeper.sence.leadMaster;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.zookeeper.sence.leadMaster.LeaderSelectorZkClient
 * @Description: 调度器
 * @create 2018/09/26 14:44
 *
 * Master 选举思路:
 * 1. 往zookeeper的master路径下写临时节点
 * 2. 其他服务关注master的删除事件
 * 3. 一旦master节点消失,开始争抢leader
 *
 */
public class LeaderSelectorZkClient {

    // 启动的服务个数
    private static final int CLIENT_QTY = 10;

    // zookeeper服务器的地址
    private static final String ZOOKEEPER_SERVERS = "10.153.2.23:2181,10.153.2.24:2181,10.153.2.26:2181";

    public static void main(String[] args) {
        // 保存所有zkClient的列表
        List<ZkClient>  clients = new ArrayList<>();

        // 保存所有服务的列表
        List<WorkServer> workServers = new ArrayList<>();

        try {
            // 模拟创建10个服务器并启动
            for ( int i = 0; i < CLIENT_QTY; ++i ) {
                // 创建zkClient
                ZkClient client = new ZkClient(ZOOKEEPER_SERVERS, 5000, 5000, new SerializableSerializer());
                clients.add(client);

                // 创建serverData
                RunningData runningData = new RunningData();
                runningData.setCid((long) i);
                runningData.setName("Client #" + i);

                // 创建服务
                WorkServer workServer = new WorkServer(runningData);
                workServer.setZkClient(client);

                workServers.add(workServer);
                workServer.start();
            }

            System.out.println("敲回车键退出！\n");
            new BufferedReader(new InputStreamReader(System.in)).readLine();

        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            System.out.println("Shutting down...");
            for (WorkServer workServer : workServers ) {
                try {
                    workServer.stop();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for ( ZkClient client : clients ) {
                try {
                    client.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
