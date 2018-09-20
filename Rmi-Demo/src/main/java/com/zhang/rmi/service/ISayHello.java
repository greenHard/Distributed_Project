package com.zhang.rmi.service;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.rmi.service.ISayHello
 * @Description: 远程接口
 * @create 2018/09/18 13:15
 */
public interface ISayHello extends Remote {

    String sayHello(String name) throws RemoteException;
}
