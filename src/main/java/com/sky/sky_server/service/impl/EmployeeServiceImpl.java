package com.sky.sky_server.service.impl;

import com.sky.sky_server.constant.MessageConstant;
import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.entity.Employee;
import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.mapper.EmployeeMapper;
import com.sky.sky_server.service.EmployeeService;
import com.sky.sky_server.utils.JwtUtil;
import com.sky.sky_server.vo.EmployeeLoginVO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeMapper employeeMapper;

    @Value("${sky.jwt.secret-key}")
    private String secretKey;

    @Value("${sky.jwt.ttl}")
    private Long ttl;

    public EmployeeServiceImpl(EmployeeMapper employeeMapper) {
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeLoginVO login(EmployeeLoginDTO employeeLoginDTO) {

        Employee employee = employeeMapper.getByUsername(employeeLoginDTO.getUsername());

        if (employee == null) {
            throw new BusinessException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        String password = DigestUtils.md5DigestAsHex(employeeLoginDTO.getPassword().getBytes());
        if (!employee.getPassword().equals(password)) {
            throw new BusinessException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == 0) {
            throw new BusinessException(MessageConstant.ACCOUNT_DISABLED);
        }

        Map<String, Object> claims = new HashMap<>();
        claims.put("employeeId", employee.getId());

        String token = JwtUtil.createJWT(secretKey, ttl, claims);
        EmployeeLoginVO employeeLoginVO = new EmployeeLoginVO();
        employeeLoginVO.setId(employee.getId());
        employeeLoginVO.setUserName(employee.getUsername());
        employeeLoginVO.setName(employee.getName());
        employeeLoginVO.setToken(token);

        return employeeLoginVO;
    }

}
