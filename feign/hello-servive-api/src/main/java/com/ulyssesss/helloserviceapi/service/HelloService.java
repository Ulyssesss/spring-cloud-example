package com.ulyssesss.helloserviceapi.service;

import com.ulyssesss.helloserviceapi.dto.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface HelloService {

    @GetMapping("hello")
    String hello(@RequestParam("p1") String p1, @RequestParam("p2") String p2);

    @GetMapping("user")
    User user();
}
