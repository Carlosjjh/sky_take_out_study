package com.sky.sky_server.dto;

import lombok.Data;

@Data
public class EmployeePageQueryDTO {
    private String name;
    private int page;
    private int pageSize;
}
