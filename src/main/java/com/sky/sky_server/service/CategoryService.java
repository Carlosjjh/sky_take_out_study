package com.sky.sky_server.service;

import com.sky.sky_server.generated.category.model.CategoryCreateRequest;
import com.sky.sky_server.dto.CategoryPageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.vo.CategoryPageVO;
import java.util.List;

public interface CategoryService {

    void save(CategoryCreateRequest categoryCreateRequest);

    PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO);

    void startOrStop(Integer status, Long id);

    List<CategoryPageVO> listEnabledByType(Integer type);
}
