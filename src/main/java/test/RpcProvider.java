package test;

import framework.RpcFramework;
import service.HelloService;
import service.impl.HelloServiceImpl;

/**
 * RpcProvider
 *
 * @Author: Heiku
 * @Date: 2019/9/20
 */
public class RpcProvider {
    public static void main(String[] args) throws Exception {

        HelloService helloService = new HelloServiceImpl();
        RpcFramework.export(helloService, 1234);
    }
}
