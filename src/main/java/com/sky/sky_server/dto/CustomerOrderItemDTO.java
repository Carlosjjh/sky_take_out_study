package com.sky.sky_server.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class CustomerOrderItemDTO {
    @NotNull
    @Positive
    private Long dishId;

    @NotNull
    @Min(1)
    private Integer number;
}
