---
title: Spring Cloud Eureka 服务治理
date: 2018-03-20 18:03:19
tags:
- Spring Cloud
categories:
- Tech
---

Spring Cloud Eureka 是对 Netflix Eureka 的二次封装，增加了 Spring Boot 风格的自动化配置，只需简单地引入依赖和注解就能提供完整的服务注册和服务发现。







<!-- more -->

## 服务中心

服务中心又称注册中心，集中式地管理各个服务的注册和发现。

在服务较多、每个服务又多份部署的的情况下，手动管理服务间的调用关系既麻烦又容易出错，其中一个服务改动，就会牵连好几个服务跟着重启。

在包含服务中心的微服务体系中，每个服务都将自身的信息注册到服务中心，各个服务间的调用都需要通过服务中心来调用，服务间调用关系有服务中心来统一管理。



## Eureka

Eureka 采用 C-S 设计架构，Eureka Server 作为服务注册、发现的服务器，系统中其他服务使用 Eureka Client 连接到 Eureka Server 并维持心跳。



### Eureka-Server 实践 

#### 1.添加 Eureka 服务端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>	
```



#### 2.使用注解启用 Eureka Server

```java
package com.ulyssesss.eurekaserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
```



#### 3.添加 Eureka 相关配置

```properties
server.port=8761
spring.application.name=eureka-server

# 多节点部署时各节点 url 用逗号分隔
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/,http://localhost:8762/eureka/

# 关闭自我保护
# 如 15 分钟内统计的心跳失败比例超过 85%，Eureka 会将所有实例信息保护起来，单机调试很容易出发保护机制
# eureka.server.enable-self-preservation=false

# 不向注册中间检索服务
# eureka.client.fetch-registry=false

# 不向注册中心注册自己
# eureka.client.register-with-eureka=false
```





### Eureka-Client 实践

#### 1.添加 eureka 客户端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```



#### 2.使用注解启用 Eureka Client

```java
package com.ulyssesss.eurekaclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class EurekaClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaClientApplication.class, args);
    }
}
```



#### 3.添加 Eureka 相关配置

```properties
spring.application.name=eureka-client
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# 服务续约任务调用间隔，默认为30秒
# eureka.instance.lease-renewal-interval-in-seconds=20

# 服务失效时间，默认为90秒
# eureka.instance.lease-expiration-duration-in-seconds=60

# 服务获取任务调用间隔，默认为30秒
# eureka.client.registry-fetch-interval-seconds=20
```



[示例代码](https://github.com/Ulyssesss/spring-cloud-example)
