package com.sky.sky_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sky.sky_server.dto.CustomerOrderDTO;
import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.OrderService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/user/order")
@Validated
public class UserOrderController {
    private final OrderService orderService;

    public UserOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Long> submit(@Valid @RequestBody CustomerOrderDTO customerOrderDTO) {
        return Result.success(orderService.submit(customerOrderDTO));
    }
}
