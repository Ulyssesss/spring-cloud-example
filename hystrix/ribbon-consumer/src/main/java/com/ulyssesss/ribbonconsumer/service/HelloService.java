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

    private static final String HELLO_SERVICE = "HTTP://hello-service/";

    @HystrixCommand(fallbackMethod = "helloFallback", ignoreExceptions = {NotFallbackException.class}
            , groupKey = "hello", commandKey = "str", threadPoolKey = "helloStr")
    public String hello(String p1, String p2) {
        return restTemplate.getForObject(HELLO_SERVICE + "hello?p1={1}&p2={2}", String.class, p1, p2);
    }

    private String helloFallback(String p1, String p2, Throwable e) {
        System.out.println("class: " + e.getClass());
        return "error, " + p1 + ", " + p2;
    }
}
