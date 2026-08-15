package com.sky.sky_server.service;

import com.sky.sky_server.dto.EmployeeDTO;
import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.vo.EmployeeLoginVO;
import com.sky.sky_server.dto.EmployeePageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.vo.EmployeePageVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

    void save(EmployeeDTO employeeDTO);

    PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO);

    void startOrStop(Integer status, Long id);

    EmployeePageVO getById(Long id);

    void update(EmployeeDTO employeeDTO);
}