package com.zhang.rmi.server;

import com.zhang.rmi.service.ISayHello;
import com.zhang.rmi.service.SayHelloImpl;

import java.net.MalformedURLException;
import java.rmi.AlreadyBoundException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.rmi.server.HelloServer
 * @Description: 服务端
 * @create 2018/09/18 13:19
 */
public class HelloServer {
    public static void main(String[] args) throws RemoteException, AlreadyBoundException, MalformedURLException {
        // 1.创建实现类
        ISayHello hello = new SayHelloImpl();

        // 2.注册服务
        LocateRegistry.createRegistry(8888);

        // 3. 绑定接口
        Naming.bind("rmi://localhost:8888/sayHello",hello);

        System.out.println("server started...");
    }
}
