package com.sky.sky_server.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmployeeLoginDTO {

    @NotBlank(message = "username must not be blank")
    @Size(max = 32, message = "username must be at most 32 characters")
    private String username;

    @NotBlank(message = "password must not be blank")
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
