package com.sky.sky_server.entity;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class Employee {
    private Long id;
    private String name;
    private String username;
    private String password;
    private String phone;
    private String sex;
    private String idNumber;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;

}
