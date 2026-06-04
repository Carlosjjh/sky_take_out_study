package com.sky.sky_server.controller;

import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.EmployeeService;
import com.sky.sky_server.vo.EmployeeLoginVO;
import com.sky.sky_server.context.BaseContext;
import com.sky.sky_server.dto.EmployeeDTO;
import com.sky.sky_server.dto.EmployeePageQueryDTO;
import com.sky.sky_server.result.PageResult;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;

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
        Long employeeId = BaseContext.getCurrentId();
        // return Result.success("token校验通过，成功访问员工信息接口");
        return Result.success("current employee id:" + employeeId);
    }

    @PostMapping
    public Result<String> save(@RequestBody EmployeeDTO employeeDTO) {
        employeeService.save(employeeDTO);
        return Result.success("save employee success");
    }

    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }
}
