---
title: Spring Cloud Ribbon 客户端负载均衡
date: 2018-03-21 16:09:44
tags:
- Spring Cloud
categories:
- Tech
---

Spring Cloud Ribbon 是基于 Netflix Ribbon 实现的客户端负载均衡工具。

Spring Cloud Ribbon 在单独使用时，可以通过在客户端中配置 ribbonServerList 来指定服务实例列表，通过轮训访问的方式起到负载均衡的作用。

在与 Eureka 联合使用时，ribbonServerList 会被重写，改为通过 Eureka 服务注册中心获取服务实例列表，可以通过简单的几行配置完成 Spring Cloud 中服务调用的负载均衡。





<!-- more -->

## 负载均衡实践

在实践客户端负载均衡之前，首先构建并启动 eureka-server，作为服务注册中心。

然后创建 hello-service 作为服务提供方，启动两个实例，分别注册到 eureka-server。

完成以上步骤之后开始构建具有负载均衡功能的服务消费方 ribbon-consumer。



### 1.添加相关依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-ribbon</artifactId>
</dependency>
```



### 2.启用服务发现客户端，声明负载均衡的 restTemplate

```java
package com.ulyssesss.ribbonconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@EnableDiscoveryClient
@SpringBootApplication
public class RibbonConsumerApplication {

    @Bean
    @LoadBalanced
    RestTemplate restTemplate() {
        return new RestTemplate();
    }

    public static void main(String[] args) {
        SpringApplication.run(RibbonConsumerApplication.class, args);
    }
}
```



### 3. 添加配置注册到服务中心

```properties
spring.application.name=ribbon-consumer
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```



### 4.编写 Controller 向 hello-service 发起请求

```java
package com.ulyssesss.ribbonconsumer.web;

import com.ulyssesss.ribbonconsumer.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

    @Autowired
    RestTemplate restTemplate;

    private static final String HELLO_SERVICE = "HTTP://hello-service/";

    @GetMapping("hello")
    public String hello() {
        // return restTemplate.getForObject(HELLO_SERVICE + "hello", String.class);
        return restTemplate.getForEntity(HELLO_SERVICE + "/hello", String.class).getBody();
    }

    @GetMapping("user")
    public User user(@RequestParam int id, @RequestParam String name) {
        User user = restTemplate.getForObject(HELLO_SERVICE + "user?id={1}&name={2}", User.class, id, name);
        System.out.println(user);
        return user;
    }

    @GetMapping("post-user")
    public String postUser() {
        return restTemplate.postForObject(HELLO_SERVICE + "user", new User(666, "AAA"), String.class);
    }
}
```



在启动 eureka-server、两个 hello-service 实例和 ribbon-consumer 后，多次访问 http://localhost:8080/hello ，可以观察到 ribbon-consumer 通过负载均衡的 restTemplate 轮训地向两个 hello-service 发起请求。



[示例代码](https://github.com/Ulyssesss/spring-cloud-example)