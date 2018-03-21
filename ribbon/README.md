# spring-cloud-ribbon



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
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# 关闭自我保护
eureka.server.enable-self-preservation=false

# 不向注册中间检索服务
eureka.client.fetch-registry=false

# 不向注册中心注册自己
eureka.client.register-with-eureka=false
```





## hello-service

### 1.添加 eureka 客户端和 web 依赖

```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>
```



### 2.`@EnableDiscoveryClient`

```java
package com.ulyssesss.helloservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class HelloServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(HelloServiceApplication.class, args);
	}
}
```



### 3. 创建 rest controller 和 相关模型

```java
package com.ulyssesss.helloservice.web;

import com.ulyssesss.helloservice.domain.User;
import org.springframework.web.bind.annotation.*;

@RestController
public class HelloController {

    @GetMapping("hello")
    public String hello() {
        System.out.println("get hello");
        return "hello, world";
    }

    @GetMapping("user")
    public User user(@RequestParam int id, @RequestParam java.lang.String name) {
        System.out.println("get user");
        return new User(id, name);
    }

    @PostMapping("user")
    public String user(@RequestBody User user) {
        System.out.println("post user");
        return user.toString();
    }
}
```

```java
package com.ulyssesss.helloservice.domain;

public class User {

    private int id;
    private String name;

    public User() {}

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```



### 4.配置 `application.properties`

```properties
spring.application.name=hello-service
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/

# 实例一
server.port=8081

# 实例二
#server.port=8082
```





## ribbon-consumer

### 1.添加依赖

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-eureka</artifactId>
</dependency>

<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-ribbon</artifactId>
</dependency>
```



### 2.启用服务发现客户端 `@EnableDiscoveryClient` ，声明负载均衡 `@LoadBalanced` 的 restTemplate

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



### 3.编写服务调用的 controller 和相关模型

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

```java
package com.ulyssesss.ribbonconsumer.domain;

public class User {

    private int id;
    private String name;

    public User() {}

    public User(int id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```



### 4.配置 `application.properties`

```properties
spring.application.name=ribbon-consumer
eureka.client.service-url.defaultZone=http://localhost:8761/eureka/
```





