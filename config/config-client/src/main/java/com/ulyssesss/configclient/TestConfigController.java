package com.ulyssesss.configclient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RefreshScope
@RestController
public class TestConfigController {

    @Value("${from}")
    private String from;

    @GetMapping("from")
    public String from() {
        return from;
    }
}
