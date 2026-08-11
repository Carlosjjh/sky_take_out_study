package com.sky.sky_server.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CategoryPageQueryDTO {

    private String name;

    @Min(value = 1, message = "type must be 1 or 2")
    @Max(value = 2, message = "type must be 1 or 2")
    private Integer type;

    @NotNull(message = "page is required")
    @Min(value = 1, message = "page must be at least 1")
    private Integer page;

    @NotNull(message = "pageSize is required")
    @Min(value = 1, message = "pageSize must be at least 1")
    private Integer pageSize;
}
