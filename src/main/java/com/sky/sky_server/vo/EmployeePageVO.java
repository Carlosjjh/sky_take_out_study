package com.sky.sky_server.vo;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class EmployeePageVO {
    private Long id;
    private String name;
    private String username;
    private String phone;
    private String sex;
    private String idNumber;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createUser;
    private Long updateUser;
}