package com.sky.sky_server.controller;

import com.sky.sky_server.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {
    @GetMapping("/hello")
    public String hello() {
        return "hello sky take out";
    }

    @GetMapping("/status")
    public Result<String> status() {
        return Result.success("sky-server is running");
    }

}
