package com.sky.sky_server.entity;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class Category {
    private Long id;
    private Integer type;
    private String name;
    private Integer sort;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}
