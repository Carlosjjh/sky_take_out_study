package com.sky.sky_server.entity;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderDetail {
    private Long id;
    private Long orderId;
    private Long dishId;
    private String name;
    private Integer number;
    private BigDecimal amount;
}
