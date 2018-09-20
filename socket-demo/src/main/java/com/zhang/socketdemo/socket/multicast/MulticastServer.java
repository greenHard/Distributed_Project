package com.zhang.socketdemo.socket.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.concurrent.TimeUnit;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.socketdemo.socket.multicast.MulticastServer
 * @Description: 广播,在组内进行广播
 * @create 2018/09/18 12:23
 */
public class MulticastServer {
    public static void main(String[] args) throws IOException, InterruptedException {
        // 地址段: 224.0.0.0 - 239.255.255.255
        InetAddress group = InetAddress.getByName("224.5.6.8");

        // 创建multicast连接
        MulticastSocket socket = new MulticastSocket();

        // 循环发送数据
        for (int i = 0; i < 10; i++) {
            String data  = "hello multicast...";
            byte[] bytes = data.getBytes();
            DatagramPacket packet = new DatagramPacket(bytes,bytes.length,group,8888);
            socket.send(packet);
            TimeUnit.SECONDS.sleep(2);
        }
    }
}
