package com.sky.sky_server.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerOrderDTO {
    @NotBlank(message = "customer name is required")
    @Size(max = 32)
    private String customerName;

    @NotBlank(message = "phone is required")
    @Pattern(regexp = "^1\\d{10}$", message = "phone must be 11 digits")
    private String phone;

    @NotBlank(message = "address is required")
    @Size(max = 128)
    private String address;

    @Valid
    @NotEmpty(message = "order items are required")
    private List<CustomerOrderItemDTO> items;
}
