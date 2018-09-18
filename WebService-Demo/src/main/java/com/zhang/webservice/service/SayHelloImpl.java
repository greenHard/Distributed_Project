package com.zhang.webservice.service;

import javax.jws.WebService;

@WebService
public class
SayHelloImpl implements ISayHello {
    @Override
    public String sayHello(String name) {
        System.out.println("call sayHello()");
        return "hello" + name + ",I am zhang";
    }
}
