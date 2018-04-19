---
title: Spring Cloud Zuul 网关（一）
date: 2018-04-17 16:19:26
tags:
- Spring Cloud
categories:
- Tech
---

利用之前提到的微服务组件，已经可以建立起一个简单的微服务系统：

* 通过 Spring Cloud Eureka 实现高可用服务注册中心
* 通过 Spring Cloud Ribbon 或 Feign 实现服务间负载均衡的接口调用
* 通过 Spring Cloud Hystrix 实现线程隔离和熔断，防止故障扩散





<!-- more -->

但是由于在微服务架构中，后端服务往往不直接开放给外部程序调用，所以需要一个 API 网关，根据请求的 Url 路由到相应的服务。API 网关负责外部应用到微服务系统之间的请求路由、负载均衡和校验过滤等功能的实现。

在 Spring Cloud 体系中，Spring Cloud Zuul 组件提供 API 网关的支持。

Spring Cloud Zuul 将自身注册为 Eureka 服务治理下的应用，从 Eureka 中获取服务实例信息，从而维护路由规则和服务实例。

同时， Zuul 提供了一套过滤器机制，通过创建过滤器对校验过滤提供支持，可以使微服务应用更专注于业务逻辑的开发。



## 网关实践

在实现网关服务的功能之前，需要搭建几个用于路由和过滤使用的微服务应用，之后就可以着手网关的构建。



### 1.添加相关依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-zuul</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```



### 2.通过注解启用网关功能

```java
package com.ulyssesss.apigateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;

@EnableZuulProxy
@SpringBootApplication
public class ApiGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
```



### 3.配置路由规则

Zuul 可以按传统路由方式进行配置，`zuul.routes.<route>.path` 配置匹配规则，`zuul.routes.<route>.url` 配置服务地址，其中 \<route\> 为路由名称，可以任意指定。

按下方配置启动应用，访问 http://localhost:5555/hello-service/hello 会转发到 http://localhost:8081/hello 。

```properties
## 服务名称、端口号、服务注册中心地址
spring.application.name=api-gateway
server.port=5555
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

## 传统路由单实例配置
zuul.routes.hello-service.path=/hello-service/**
zuul.routes.hello-service.url=http://localhost:8081/
```



按传统路由方式进行多服务实例配置时，需关闭负载均衡和 Eureka 的结合，通过配置 Ribbon 的服务列表进行负载均衡。

```properties
## 传统路由多实例配置
zuul.routes.hello-service.path=/hello-service/**
zuul.routes.hello-service.service-id=hello-service
ribbon.eureka.enabled=false
hello-service.ribbon.listOfServers=http://localhost:8081/,http://localhost:8082/
```



传统配置方式需要花费大量时间维护 path 和 url 的关系，在与 Eureka 结合后，可以通过服务发现机制自动维护映射关系。

```properties
## 面向服务的路由
zuul.routes.hello-service.path=/hello-service/**
zuul.routes.hello-service.service-id=hello-service

## 面向服务路由的简介配置
## zuul.routes.<serviceId>=<path>
zuul.routes.feign-consumer=/feign-consumer/**
```



由于大部分的路由配置规则都会采用服务名作为外部请求的前缀，所以 Spring Cloud Zuul 为 Eureka 中的每一个服务都自动创建一个默认的路由规则，如同上面的面上服务的路由。要排除指定服务的默认规则，需要添加如下配置。

```properties
## 排除指定服务的默认规则
zuul.ignored-services=hello-service,feign-consumer

## 关闭所有服务的默认规则
#zuul.ignored-services=*
```



以上全部配置如下：

```properties
spring.application.name=api-gateway
server.port=5555
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

## 传统路由单实例配置
#zuul.routes.hello-service.path=/hello-service/**
#zuul.routes.hello-service.url=http://localhost:8081/

## 传统路由多实例配置
#zuul.routes.hello-service.path=/hello-service/**
#zuul.routes.hello-service.service-id=hello-service
#ribbon.eureka.enabled=false
#hello-service.ribbon.listOfServers=http://localhost:8081/,http://localhost:8082/

## 面向服务的路由
## zuul.routes.<route>.path 指定请求路径
## zuul.routes.<route>.serviceId 指定服务名称
## <route> 为路由名称，可任意指定
#zuul.routes.hello-service.path=/hello-service/**
#zuul.routes.hello-service.service-id=hello-service

## 面向服务路由的简洁配置
## zuul.routes.<serviceId>=<path>
#zuul.routes.feign-consumer=/feign-consumer/**

## 外部请求前缀作为服务名为 zuul 默认规则，上方面向服务路由其实都可以省略

## 排除指定服务的默认规则
#zuul.ignored-services=hello-service,feign-consumer

## 关闭默认规则
#zuul.ignored-services=*
```



[示例代码](https://github.com/Ulyssesss/spring-cloud-example)