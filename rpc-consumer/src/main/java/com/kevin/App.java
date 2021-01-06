package com.kevin;

import com.kevin.minirpc.consumer.ConsumerBoot;
import com.kevin.service.IUserService;

/**
 * @author kevin.lee
 * @date 2021/1/6 0006
 */
public class App {

    public static void main(String[] args) throws InterruptedException {

        //1.创建代理对象
        IUserService service = (IUserService) ConsumerBoot.remote(IUserService.class);

        //2.循环给服务器写数据
        while (true) {
            String result = service.sayHello("are you ok !!are you ok !!are you ok !!");
            System.out.println(result);
            Thread.sleep(1000);
        }

    }
}
