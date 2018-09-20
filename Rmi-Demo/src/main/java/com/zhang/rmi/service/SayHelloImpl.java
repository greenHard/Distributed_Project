package com.zhang.rmi.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * @author <p>zhangyuyang160@gmail.com<p>
 * @ClassName: com.zhang.rmi.service.SayHelloImpl
 * @Description: 实现远程接口，并且继承UnicastRemoteObject
 * @create 2018/09/18 13:17
 */
public class SayHelloImpl extends UnicastRemoteObject implements ISayHello {

    public SayHelloImpl() throws RemoteException {
    }

    @Override
    public String sayHello(String name)  throws RemoteException{
        return name + " say-> hello rmi";
    }
}
