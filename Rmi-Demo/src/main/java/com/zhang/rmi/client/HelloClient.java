package com.zhang.rmi.client;

import com.zhang.rmi.service.ISayHello;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.rmi.client.HelloClient
 * @Description: 客户端
 * @create 2018/09/18 13:19
 */
public class HelloClient {
    public static void main(String[] args) throws RemoteException, NotBoundException, MalformedURLException {
        // 1. 寻找服务
        ISayHello sayHello = (ISayHello) Naming.lookup("rmi://localhost:8888/sayHello");

        System.out.println(sayHello);
        System.out.println(sayHello.sayHello("zhang"));
    }
}
