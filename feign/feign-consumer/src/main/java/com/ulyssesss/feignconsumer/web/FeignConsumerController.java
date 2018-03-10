package com.ulyssesss.feignconsumer.web;

import com.ulyssesss.feignconsumer.service.RefactorHelloService;
import com.ulyssesss.helloserviceapi.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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

    @RequestMapping("user")
    public User user() {
        System.out.println("feign consumer get user");
        return refactorHelloService.user();
    }
}
