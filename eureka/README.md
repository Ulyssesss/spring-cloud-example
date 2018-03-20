# spring-cloud-eureka



## eureka-server 

### 1.添加 eureka 服务端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka-server</artifactId>
</dependency>	
```



### 2.`@EnableEurekaServer`

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



### 3.配置 `application.properties`

```properties
server.port=8761
spring.application.name=eureka-server
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/,http://localhost:8762/eureka/

# 关闭自我保护
# eureka.server.enable-self-preservation=false

# 不向注册中间检索服务
# eureka.client.fetch-registry=false

# 不向注册中心注册自己
# eureka.client.register-with-eureka=false
```





## eureka-client

### 1.添加 eureka 客户端依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>
```



### 2.`@EnableDiscoveryClient`

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



### 3.配置 `application.properties`

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