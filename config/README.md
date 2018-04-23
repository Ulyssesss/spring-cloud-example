---
title: Spring Cloud Config 配置中心
date: 2018-04-23 12:17:11
tags:
- Spring Cloud
categories:
- Tech
---

Spring Cloud Config 用于为分布式系统中的基础设施和微服务应用提供集中化的外部配置支持，分为服务端和客户端。

服务端为分布式配置中心，是一个独立的微服务应用；客户端为分布式系统中的基础设置或微服务应用，通过指定配置中心来管理相关的配置。

Spring Cloud Config 构建的配置中心，除了适用于 Spring 构建的应用外，也可以在任何其他语言构建的应用中使用。

Spring Cloud Config 默认采用 Git 存储配置信息，天然支持对配置信息的版本管理。





<!-- more -->

## 构建配置中心

创建 Spring Boot 工程 config-server，然后按以下步骤完成配置中心的构建。



### 1.添加依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-config-server</artifactId>
</dependency>
```



### 2.通过注解启用配置中心

```java
package com.ulyssesss.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```



### 3.修改配置

```properties
spring.application.name=config-server
server.port=7001

# Git 仓库位置
spring.cloud.config.server.git.uri=https://github.com/Ulyssesss/spring-cloud-config-example.git

# 仓库路径下相对搜索位置，可配置多个
spring.cloud.config.server.git.search-paths=config

# 访问 Git 仓库的用户名
spring.cloud.config.server.git.username=

# 访问 Git 仓库的密码
spring.cloud.config.server.git.password=
```



### 4.创建配置仓库并提交修改

创建 Git 仓库及 config 目录，添加 `ulyssesss.properties`、`ulyssesss-dev.properties`，在配置中分别添加 `from=default-1.0` 和 `from=dev-1.0` 。

创建 `config-label-test` 分支，将配置文件中的版本号 `1.0` 修改为 `2.0` 。

提交修改并推送至远程仓库后启动 config-server，可按照以下规则访问配置信息：

* / {application} / {profile} [ / {label} ]
* / {application} - {profile} . yml
* / {label} / {application} - {profile} . yml
* / {application} - {profile} . properties
* / {label} / {application} - {profile} . properties





## 客户端获取配置

创建Spring Boot 工程 config-client ，按以下步骤编写客户端：



### 1.添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-config</artifactId>
</dependency>
```



### 2.增加配置

创建 bootstrap.properties，并增加配置中心的相关配置。

```properties
spring.application.name=ulyssesss
spring.cloud.config.profile=dev
spring.cloud.config.label=config-label-test
spring.cloud.config.uri=http://localhost:7001
```
注意，以上属性必须配置在 bootstrap.properties 中。

由于 Spring Boot 应用会优先加载应用 jar 包以外的配置，而通过 bootstrap.properties 对 config-server 的配置会使应用从 config-server 中获取外部配置，优先级比本地配置高。



### 3.创建控制器查看配置

创建 ConfigClientController，通过访问 http://localhost:8080/from 获取配置。

```java
package com.ulyssesss.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestConfigController {

    @Value("${from}")
    private String from;

    @GetMapping("from")
    public String from() {
        return from;
    }
}
```



访问 http://localhost:8080/from ，得到返回结果 `from=dev-2.0` 。修改配置中的 `spring.cloud.config.profile` 和 `spring.cloud.config.label` ，重启应用再次访问链接会得到相应的配置信息。



## 服务模式高可用配置

传统模式的高可用不需要额外的配置，只需将所有的 config-server 实例全部指向同一个 Git 仓库，客户端指定 config-server 时指向上层负载均衡设备地址。

服务模式通过将 config-server 纳入 Eureka 服务治理体系，将 config-server 注册成为一个微服务应用，客户端通过服务名从服务注册中心获取配置中心的实例信息。



### 1.添加服务端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```



### 2.注解启用服务发现功能

```java
package com.ulyssesss.configserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.config.server.EnableConfigServer;

@EnableConfigServer
@EnableDiscoveryClient
@SpringBootApplication
public class ConfigServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
```



### 3.添加服务端配置

```properties
spring.application.name=config-server
server.port=7001

# Git 仓库位置
spring.cloud.config.server.git.uri=https://github.com/Ulyssesss/spring-cloud-config-example.git

# 仓库路径下相对搜索位置，可配置多个
spring.cloud.config.server.git.search-paths=config

# 访问 Git 仓库的用户名
spring.cloud.config.server.git.username=

# 访问 Git 仓库的密码
spring.cloud.config.server.git.password=

# 服务注册中心地址
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```



### 4.添加客户端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```



### 5.客户端启用服务发现功能

```java
package com.ulyssesss.configclient;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ConfigClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigClientApplication.class, args);
    }
}

```



### 6.修改客户端配置

```properties
spring.application.name=ulyssesss
spring.cloud.config.profile=test
spring.cloud.config.label=config-label-test
#spring.cloud.config.uri=http://localhost:7001

# 服务注册中心地址
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
# 启用配置客户端的服务发现功能
spring.cloud.config.discovery.enabled=true
# 指定配置中心的服务名
spring.cloud.config.discovery.service-id=config-server
```



## 动态刷新配置

有时需要对配置内容进行实时更新，Spring Cloud Config 通过 actuator 可实现此功能。



### 1.客户端添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-actuator</artifactId>
</dependency>
```



### 2.添加刷新范围注解

```java
package com.ulyssesss.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class TestConfigController {

    @Value("${from}")
    private String from;

    @GetMapping("from")
    public String from() {
        return from;
    }
}
```



修改配置中 from 的值，提交到远程仓库，通过 post 方法访问 http://localhost:8080/refresh 即可刷新配置项的值。

注意，spring boot 1.5 以上会默认开启安全认证，可通过一下配置关闭安全认证。

```properties
# 关闭安全认证
management.security.enabled=false
```



[示例代码](https://github.com/Ulyssesss/spring-cloud-example) 欢迎 Star 