package com.sky.sky_server.service.impl;

import com.sky.sky_server.constant.MessageConstant;
import com.sky.sky_server.dto.EmployeeLoginDTO;
import com.sky.sky_server.entity.Employee;
import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.mapper.EmployeeMapper;
import com.sky.sky_server.service.EmployeeService;
import com.sky.sky_server.utils.JwtUtil;
import com.sky.sky_server.vo.EmployeeLoginVO;
import com.sky.sky_server.context.BaseContext;
import com.sky.sky_server.dto.EmployeeDTO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.sky_server.dto.EmployeePageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.vo.EmployeePageVO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

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

    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        employee.setUsername(employeeDTO.getUsername());
        employee.setName(employeeDTO.getName());
        employee.setPhone(employeeDTO.getPhone());
        employee.setSex(employeeDTO.getSex());
        employee.setIdNumber(employeeDTO.getIdNumber());

        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
        employee.setStatus(1);

        employee.setCreateTime(LocalDateTime.now());
        employee.setUpdateTime(LocalDateTime.now());
        employee.setCreateUser(BaseContext.getCurrentId());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.insert(employee);
    }

    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        PageHelper.startPage(employeePageQueryDTO.getPage(), employeePageQueryDTO.getPageSize());

        Page<EmployeePageVO> page = (Page<EmployeePageVO>) employeeMapper.pageQuery(employeePageQueryDTO);

        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        Employee employee = new Employee();

        employee.setId(id);
        employee.setStatus(status);
        employee.setUpdateTime(LocalDateTime.now());
        employee.setUpdateUser(BaseContext.getCurrentId());

        employeeMapper.update(employee);
    }

    @Override
    public EmployeePageVO getById(Long id) {
        return employeeMapper.getById(id);
    }

}
