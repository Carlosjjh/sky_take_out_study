package com.sky.sky_server.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class EmployeeDTO {
    @NotBlank(message = "username must not be blank")
    @Size(max = 32, message = "username must be at most 32 characters")
    private String username;

    @NotBlank(message = "name must not be blank")
    @Size(max = 32, message = "name must be at most 32 characters")
    private String name;

    @Size(max = 11, message = "phone must be at most 11 characters")
    private String phone;

    @Size(max = 2, message = "sex must be at most 2 characters")
    private String sex;

    @Size(max = 18, message = "idNumber must be at most 18 characters")
    private String idNumber;
    private Long id;
}
