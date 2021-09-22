package com.zheng.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;
import tk.mybatis.spring.annotation.MapperScan;

@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
//扫描mybatis通用mapper所在包
@MapperScan(basePackages = "com.zheng.order.mapper")
//扫描所有包以及相关组件包
@ComponentScan(basePackages = {"com.zheng","org.n3r.idworker"})
@EnableDiscoveryClient
@EnableFeignClients(basePackages = {
        "com.zheng.user.service",
        "com.zheng.item.service"
})
@EnableScheduling
public class OrderApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderApplication.class,args);
    }
}
