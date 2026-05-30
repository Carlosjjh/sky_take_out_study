package com.sky.sky_server.service;

import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.vo.EmployeeLoginVO;

public interface EmployeeService {

    EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO);

}