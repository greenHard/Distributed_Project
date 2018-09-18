package com.zhang.webservice.service;

import javax.xml.ws.Endpoint;

/**
 * 发布服务
 */
public class PublishDemo {
    public static void main(String[] args) {
        // 发布服务
        Endpoint.publish("http://localhost:8081/hello",new SayHelloImpl());
        System.out.println("publish success");
    }
}
