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
