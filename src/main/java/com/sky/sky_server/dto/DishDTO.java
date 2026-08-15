package com.sky.sky_server.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DishDTO {
    @NotBlank(message = "dish name is required")
    @Size(max = 32)
    private String name;

    @NotNull(message = "dish category is required")
    @Positive
    private Long categoryId;

    @NotNull(message = "dish price is required")
    @DecimalMin(value = "0.01", message = "dish price must be positive")
    private BigDecimal price;

    @Size(max = 512)
    private String image;

    @Size(max = 255)
    private String description;
}
