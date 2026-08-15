package com.sky.sky_server.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class CustomerOrder {
    private Long id;
    private String customerName;
    private String phone;
    private String address;
    private BigDecimal amount;
    private Integer status;
    private LocalDateTime orderTime;
}
