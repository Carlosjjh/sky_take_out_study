package com.sky.sky_server.service;

import java.util.List;

import com.sky.sky_server.dto.DishDTO;
import com.sky.sky_server.dto.DishPageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.vo.DishCustomerVO;

public interface DishService {
    Long save(DishDTO dishDTO);

    PageResult pageQuery(DishPageQueryDTO queryDTO);

    void startOrStop(Integer status, Long id);

    List<DishCustomerVO> listEnabledByCategoryId(Long categoryId);
}
