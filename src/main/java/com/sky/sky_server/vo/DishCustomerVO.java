package com.sky.sky_server.vo;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class DishCustomerVO {
    private Long id;
    private String name;
    private Long categoryId;
    private BigDecimal price;
    private String image;
    private String description;
}
