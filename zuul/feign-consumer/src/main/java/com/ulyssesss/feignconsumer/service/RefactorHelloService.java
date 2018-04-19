package com.ulyssesss.feignconsumer.service;

import com.ulyssesss.helloserviceapi.service.HelloService;
import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient(name = "hello-service", fallback = HelloServiceFallback.class)
public interface RefactorHelloService extends HelloService {
}
