package com.zhang.socketdemo.socket.tcp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.socketdemo.socket.tcp.SocketServer
 * @Description: socket服务端
 * @create 2018/09/13 15:29
 */
public class SocketServer {

    public static void main(String[] args){
        ServerSocket serverSocket;
        try {
            // 启动一个服务
            serverSocket = new ServerSocket(8888);

            // 循环接收数据
            while (true) {
                // 等待接收一个请求
                Socket accept = serverSocket.accept();

                // 启动一个线程循环读取数据
                new Thread(() -> {
                    try {
                        // 读取数据
                        BufferedReader reader = new BufferedReader(new InputStreamReader(accept.getInputStream()));
                        // 发送数据
                        PrintWriter writer = new PrintWriter(accept.getOutputStream(), true);
                        while(true) {
                            // 读取客户端的数据
                            String clientData = reader.readLine();

                            if(clientData==null){
                                break;
                            }

                            System.out.println(clientData);

                            // 响应数据
                            writer.println("hello client, I am server...");
                            writer.flush();
                        }

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }).start();


            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
