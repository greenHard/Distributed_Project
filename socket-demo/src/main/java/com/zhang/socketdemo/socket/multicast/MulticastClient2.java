package com.zhang.socketdemo.socket.multicast;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.socketdemo.socket.multicast.MulticastClient1
 * @Description: 广播客户端
 * @create 2018/09/18 12:29
 */
public class MulticastClient2 {
    public static void main(String[] args) throws IOException {
        // 创建组
        InetAddress group = InetAddress.getByName("224.5.6.8");
        // 创建socket连接
        MulticastSocket socket = new MulticastSocket(8888);
        // 加入指定的组中
        socket.joinGroup(group);

        byte[] buf = new byte[256];
        // 循环读取数据
        while (true) {
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            socket.receive(packet);
            System.out.println("接收到的数据为: " + new String(packet.getData()));
        }
    }
}
