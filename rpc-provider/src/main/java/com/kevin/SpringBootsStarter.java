package com.kevin;

import com.kevin.minirpc.annotation.SupportRPC;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * @author kevin.lee
 * @date 2021/1/3 0003
 */

@SpringBootApplication
@EnableCaching
@SupportRPC
public class SpringBootsStarter {
    public static void main(String[] args) {
        SpringApplication.run(SpringBootsStarter.class, args);
    }
}
