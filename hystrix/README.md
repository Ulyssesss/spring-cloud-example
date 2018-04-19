---
title: Spring Cloud Hystrix 容错保护
date: 2018-04-09 09:56:14
tags:
- Spring Cloud
categories:
- Tech
---

在微服务架构中，系统被拆分成很多个服务单元，各个服务单元的应用通过 HTTP 相互调用、依赖。

在某个服务由于网络或其他原因自身出现故障、延迟时，调用方也会出现延迟。若调用方请求不断增加，可能会形成任务积压，最终导致调用方服务瘫痪，服务不可用现象逐渐放大。





<!-- more -->

## Spring Cloud Hystrix

针对上述问题，Spring Cloud Hystrix 实现了一系列服务保护措施，从而实现服务降级、服务熔断等功能，对延迟和故障提供强大的容错能力。

Hystrix 有以下主要特性：

* 服务熔断

> Hystrix 会记录各个服务的请求信息，通过 `成功`、`失败`、`拒绝`、`超时` 等统计信息判断是否打开断路器，将某个服务的请求进行熔断。一段时间后切换到半开路状态，如果后面的请求正常则关闭断路器，否则继续打开断路器。

* 服务降级

> 服务降级是请求失败时的后备方法，故障时执行降级逻辑。

* 线程隔离

> Hystrix 通过线程池实现资源的隔离，确保对某一服务的调用在出现故障时不会对其他服务造成影响。



## 容错实践

首先启动之前提到的服务注册中心 eureka-server、服务提供方 hello-service 和服务消费放 ribbon-consumer，然后改造 ribbon-consumer 使其具备容错能力。



### 1.添加相关依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-hystrix</artifactId>
</dependency>
```



### 2.配置 hystrix 超时时间

```properties
spring.application.name=ribbon-consumer
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# hystrix command 请求执行超时进入降级逻辑
hystrix.command.default.execution.isolation.thread.timeoutInMilliseconds=1000
```



### 3.编写 service 完成服务调用

```java
package com.ulyssesss.ribbonconsumer.service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.ulyssesss.ribbonconsumer.exception.NotFallbackException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class HelloService {

    @Autowired
    RestTemplate restTemplate;

    private static final String HELLO_SERVICE = "http://hello-service/";

    @HystrixCommand(fallbackMethod = "helloFallback", ignoreExceptions = {NotFallbackException.class}
            , groupKey = "hello", commandKey = "str", threadPoolKey = "helloStr")
    public String hello(String p1, String p2) {
        return restTemplate.getForObject(HELLO_SERVICE + "hello", String.class, p1, p2);
    }

    private String helloFallback(String p1, String p2, Throwable e) {
        System.out.println("class: " + e.getClass());
        return "error, " + p1 + ", " + p2;
    }
}
```

其中 fallbackMethod 指定处理降级逻辑的方法，ignoreExceptions 指定不执行降级逻辑的异常，groupKey、commandKey 作为命令统计的分组及命令名称，threadPoolKey 用于指定线程池的划分。



### 4.Controller 通过 HelloService 完成请求

```java
package com.ulyssesss.ribbonconsumer.web;

import com.ulyssesss.ribbonconsumer.service.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConsumerController {

    @Autowired
    HelloService helloService;

    @GetMapping("hello")
    public String hello(@RequestParam String p1, @RequestParam String p2) {
        System.out.println("hello");
        return helloService.hello(p1, p2);
    }
}
```



### 5.调整 hello-service 使其便于测试

```java
package com.ulyssesss.helloservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
public class HelloController {

    @GetMapping("hello")
    public String hello(@RequestParam String p1, @RequestParam String p2) throws Exception {
        int sleepTime = new Random().nextInt(2000);
        System.out.println("hello sleep " + sleepTime);
        Thread.sleep(sleepTime);
        return "hello, " + p1 + ", " + p2;
    }
}
```



完成上述步骤后启动应用，访问 http://localhost:8080/hello?p1=1&p2=2 ，正常情况下响应为 `hello, 1, 2` ，关闭 hello-service 或在 sleepTime 超过 1000ms 时，执行降级逻辑，返回 `error, 1, 2` 。



[示例代码](https://github.com/Ulyssesss/spring-cloud-example)