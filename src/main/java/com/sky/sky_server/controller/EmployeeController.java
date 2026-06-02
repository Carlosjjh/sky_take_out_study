package com.sky.sky_server.controller;

import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.EmployeeService;
import com.sky.sky_server.vo.EmployeeLoginVO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/admin/employee")
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        EmployeeLoginVO employeeLoginVO = employeeService.login(employeeLoginDTO);
        return Result.success(employeeLoginVO);
    }

    @GetMapping("/profile")
    public Result<String> profile() {
        return Result.success("token校验通过，成功访问员工信息接口");
    }
}
