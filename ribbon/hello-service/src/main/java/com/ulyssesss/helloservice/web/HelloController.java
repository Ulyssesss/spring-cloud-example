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
    public String user(@RequestBody User user) throws InterruptedException {
        System.out.println("post user");
        return user.toString();
    }
}
