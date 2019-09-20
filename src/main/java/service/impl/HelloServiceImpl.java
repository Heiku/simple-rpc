package service.impl;

import service.HelloService;

/**
 * @Author: Heiku
 * @Date: 2019/9/20
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) {
        return "hello " + name;
    }
}
