package com.ulyssesss.feignconsumer.service;

import com.ulyssesss.helloserviceapi.service.HelloService;
import org.springframework.cloud.netflix.feign.FeignClient;

@FeignClient("hello-service")
public interface RefactorHelloService extends HelloService {
}
