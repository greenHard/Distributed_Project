package com.zhang.socketdemo.socket.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.socketdemo.socket.tcp.SocketClient
 * @Description: socket客户端
 * @create 2018/09/13 15:28
 */
public class SocketClient {
    public static void main(String[] args) {
        try {
            // 1. 创建socket
            Socket socket = new Socket("localhost", 8888);
            // 2. 创建reader,读取服务端信息
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 发送消息给服务端,自动刷新
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            writer.println("hello server ,I am client...");

            // 循环拉取数据
            while (true) {
                // 读取服务器端发送数据
                String serverData = reader.readLine();
                if (serverData == null) {
                    break;
                }
                // 输出数据
                System.out.println(serverData);
            }

            writer.close();
            socket.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
