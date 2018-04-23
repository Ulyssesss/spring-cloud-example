---
title: Spring Cloud Feign 声明式服务调用
date: 2018-04-13 09:40:44
tags:
- Spring Cloud
categories:
- Tech
---

在微服务的实践过程中，Spring Cloud Ribbon 和 Spring Cloud Hystrix 通常一起使用。

Spring Cloud Feign 是对这两个基础工具的更高层次封装，在 Netflix Feign 的基础上扩展了对 Spring MVC 的注解支持，提供了一种声明式的 Web 服务客户端定义方式。





<!-- more -->

## 快速入门

启动服务注册中心 eureka-server 及服务提供方 hello-service，创建 spring boot 工程 feign-consumer。



### 1.添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-feign</artifactId>
</dependency>
```



### 2.注解启用 Feign

```java
package com.ulyssesss.feignconsumer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;

@EnableFeignClients
@EnableDiscoveryClient
@SpringBootApplication
public class FeignConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(FeignConsumerApplication.class, args);
    }
}
```



### 3.定义 HelloService 接口

```java
package com.ulyssesss.feignconsumer.service;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("hello-service")
public interface HelloService {

    @GetMapping("hello")
    String hello(@RequestParam("p1") String p1, @RequestParam("p2") String p2);
}
```

其中 @FeignClient 指定服务名，Spring MVC 注解绑定具体的 REST 接口及请求参数。

注意在定义参数绑定时，@RequestParam 、@RequestHeader 等注解的 value 不能省略，Spring MVC 会根据参数名作为默认值，但 Feign 中必须通过 value 指定。



### 4.编写 Controller

```java
package com.ulyssesss.feignconsumer.web;

import com.ulyssesss.feignconsumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeignConsumerController {

    @Autowired
    HelloService helloService;

    @GetMapping("hello")
    public String hello(@RequestParam String p1, @RequestParam String p2) {
        System.out.println("feign consumer get hello");
        return helloService.hello(p1, p2);
    }
}
```



### 5.配置服务注册中心地址

```properties
spring.application.name=feign-consumer
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```



启动全部应用，访问 http://localhost:8080/hello?p1=a&p2=b ，feign-consumer 以声明式的服务调用访问 hello-service，返回 `hello, a, b` 。



## 继承特性

当使用 Spring MVC 注解来绑定服务接口时，几乎可以完全从服务提供方的 Controller 中复制，所以能够利用 Feign 的继承特性做进一步抽象，复用 REST 接口定义，减少编码量。



### 1.添加依赖

创建 Maven 工程 hello-service-api，由于需要用到 Spring MVC 的注解，所以添加 spring-boot-starter-web 依赖。

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
    <scope>provided</scope>
</dependency>
```



### 2.定义接口及 DTO

在 hello-service-api 中定义能够复用的 DTO 和接口定义。

```java
package com.ulyssesss.helloserviceapi.service;

import com.ulyssesss.helloserviceapi.dto.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface HelloService {

    @GetMapping("hello")
    String hello(@RequestParam("p1") String p1, @RequestParam("p2") String p2);

    @GetMapping("user")
    User user();

    @PostMapping("post")
    String post();
}
```

```java
package com.ulyssesss.helloserviceapi.dto;

public class User {
    
    private String name;
    private int age;

    public User() {}

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" + "name='" + name + '\'' + ", age=" + age + '}';
    }
    // set get
}
```



### 3.改写 hello-service

在 hello-service 中引入 hello-service-api 依赖，重写 HelloController。

```java
package com.ulyssesss.helloservice.web;

import com.ulyssesss.helloserviceapi.dto.User;
import com.ulyssesss.helloserviceapi.service.HelloService;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController implements HelloService {

    @Override
    public String hello(@RequestParam String p1, @RequestParam String p2) {
        System.out.println("hello service get hello");
        return "hello, " + p1 + ", " + p2;
    }

    @Override
    public User user() {
        System.out.println("hello service get user");
        return new User("Jack", 22);
    }

    @Override
    public String post() {
        System.out.println("hello service post");
        return "post";
    }
}
```



### 4.改写 feign-consumer

在 feign-consumer 中引入 hello-service-api 依赖，创建 RefactorHelloService 继承自 HelloService。

```java
package com.ulyssesss.feignconsumer.service;

import com.ulyssesss.helloserviceapi.service.HelloService;
import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(name = "hello-service")
public interface RefactorHelloService extends HelloService {
}
```

修改 FeignConsumerController，注入 RefactorHelloService。

```java
package com.ulyssesss.feignconsumer.web;

import com.ulyssesss.feignconsumer.service.RefactorHelloService;
import com.ulyssesss.helloserviceapi.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FeignConsumerController {

    @Autowired
    RefactorHelloService refactorHelloService;

    @GetMapping("hello")
    public String hello(@RequestParam String p1, @RequestParam String p2) {
        System.out.println("feign consumer get hello");
        return refactorHelloService.hello(p1, p2);
    }

    @GetMapping("user")
    public User user() {
        System.out.println("feign consumer get user");
        return refactorHelloService.user();
    }

    @PostMapping("post")
    public String post() {
        System.out.println("feign consumer post");
        return refactorHelloService.post();
    }
}
```



启动全部应用，访问 http://localhost:8080/hello?p1=a&p2=b ，feign-consumer 以声明式的服务调用访问 hello-service，返回 `hello, a, b` 。

使用 Spring Cloud Feign 的继承特性，可以将接口从 Controller 中玻璃，配合 Maven 私有仓库可以实现接口定义的共享，减少服务消费方的绑定配置。



## Ribbon 及 Hystrix 配置

Spring Cloud Feign 的客户端负载均衡通过 Spring Cloud Ribbon 实现，通过配置 Ribbon 可以定义客户端调用参数。Riibon 和 Hystrix 的配置如下：

```properties
spring.application.name=feign-consumer
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

## 启用 hystrix
feign.hystrix.enabled=true
## 全局超时熔断时间
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=10000

## 全局连接超时时间
ribbon.ConnectTimeout=250
## 全局接口调用超时时间
ribbon.ReadTimeout=10000
## 全局重试所有请求（POST 请求等）开关
ribbon.OkToRetryOnAllOperations=false

## 针对 hello-service 服务，重试切换的实例数
hello-service.ribbon.MaxAutoRetriesNextServer=1
## 针对 hello-service 服务，对当前实例重试次数
hello-service.ribbon.MaxAutoRetries=0
```



## 服务降级

Hystrix 提供的服务降级是容错的重要方法，由于 Feign 在定义服务客户端时将 HystrixCommand 的定义进行了封装，导致无法使用 @HystrixCommand 的 fallback 参数指定降级逻辑。

Spring Cloud Feign 提供了一种简单的定义服务降级的方式，创建 HelloServiceFallback 实现 HelloService 接口，通过 @Component 声明为 Bean，在 HelloServiceFallback 中实现具体的降级逻辑，最后在 @FeignClient 中通过 fallback 属性声明处理降级逻辑的 Bean。

```java
package com.ulyssesss.feignconsumer.service;

import com.ulyssesss.helloserviceapi.dto.User;
import org.springframework.stereotype.Component;

@Component
public class HelloServiceFallback implements RefactorHelloService {

    @Override
    public String hello(String p1, String p2) {
        return "error";
    }

    @Override
    public User user() {
        return new User("error", 0);
    }

    @Override
    public String post() {
        return "error";
    }
}
```

```java
package com.ulyssesss.feignconsumer.service;

import com.ulyssesss.helloserviceapi.service.HelloService;
import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(name = "hello-service", fallback = HelloServiceFallback.class)
public interface RefactorHelloService extends HelloService {
}
```



启动服务后断开服务提供方 hello-service，访问 feign-consumer 接口，feign-consumer 会按照 HelloServiceFallback 中的定义执行降级逻辑。



[示例代码](https://github.com/Ulyssesss/spring-cloud-example) 欢迎 Star 