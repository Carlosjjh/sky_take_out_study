package com.sky.sky_server.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.sky_server.context.BaseContext;
import com.sky.sky_server.dto.DishDTO;
import com.sky.sky_server.dto.DishPageQueryDTO;
import com.sky.sky_server.entity.Dish;
import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.mapper.CategoryMapper;
import com.sky.sky_server.mapper.DishMapper;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.service.DishService;
import com.sky.sky_server.vo.DishCustomerVO;
import com.sky.sky_server.vo.DishPageVO;

@Service
public class DishServiceImpl implements DishService {
    private final DishMapper dishMapper;
    private final CategoryMapper categoryMapper;

    public DishServiceImpl(DishMapper dishMapper, CategoryMapper categoryMapper) {
        this.dishMapper = dishMapper;
        this.categoryMapper = categoryMapper;
    }

    @Override
    public Long save(DishDTO dishDTO) {
        if (categoryMapper.listEnabledByType(1).stream().noneMatch(category -> category.getId().equals(dishDTO.getCategoryId()))) {
            throw new BusinessException("dish category does not exist or is disabled");
        }
        Dish dish = new Dish();
        dish.setName(dishDTO.getName().trim());
        dish.setCategoryId(dishDTO.getCategoryId());
        dish.setPrice(dishDTO.getPrice());
        dish.setImage(blankToNull(dishDTO.getImage()));
        dish.setDescription(blankToNull(dishDTO.getDescription()));
        dish.setStatus(1);
        dish.setCreateTime(LocalDateTime.now());
        dish.setUpdateTime(LocalDateTime.now());
        dish.setCreateUser(BaseContext.getCurrentId());
        dish.setUpdateUser(BaseContext.getCurrentId());
        dishMapper.insert(dish);
        return dish.getId();
    }

    @Override
    public PageResult pageQuery(DishPageQueryDTO queryDTO) {
        PageHelper.startPage(queryDTO.getPage(), queryDTO.getPageSize());
        Page<DishPageVO> page = (Page<DishPageVO>) dishMapper.pageQuery(queryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        dishMapper.updateStatus(status, id, LocalDateTime.now(), BaseContext.getCurrentId());
    }

    @Override
    public List<DishCustomerVO> listEnabledByCategoryId(Long categoryId) {
        return dishMapper.listEnabledByCategoryId(categoryId);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
