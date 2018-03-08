package com.ulyssesss.helloservice.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    @GetMapping("hello")
    public String hello() {
        System.out.println("get hello");
        return "hello, world";
    }
}
