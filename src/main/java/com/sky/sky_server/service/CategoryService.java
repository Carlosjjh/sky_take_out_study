package com.sky.sky_server.service;

import com.sky.sky_server.generated.category.model.CategoryCreateRequest;
import com.sky.sky_server.dto.CategoryPageQueryDTO;
import com.sky.sky_server.result.PageResult;

public interface CategoryService {

    void save(CategoryCreateRequest categoryCreateRequest);

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStop(Integer status, Long id);
}
