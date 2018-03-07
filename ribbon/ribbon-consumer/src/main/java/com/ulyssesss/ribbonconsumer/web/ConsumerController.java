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
