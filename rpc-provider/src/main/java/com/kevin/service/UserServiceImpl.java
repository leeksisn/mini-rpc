package com.kevin.service;

import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements IUserService {

    //将来客户单要远程调用的方法
    public String sayHello(String msg) {
        return "您调用了sayHello，参数是： : " + msg;
    }
}
