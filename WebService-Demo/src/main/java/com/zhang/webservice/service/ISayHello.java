package com.zhang.webservice.service;

import javax.jws.WebMethod;
import javax.jws.WebService;

@WebService //SE和SEI实现类
public interface ISayHello {
    @WebMethod
    String sayHello(String name);
}
