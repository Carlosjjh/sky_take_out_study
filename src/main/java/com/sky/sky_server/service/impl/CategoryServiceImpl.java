package com.sky.sky_server.service.impl;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;

import com.sky.sky_server.context.BaseContext;
import com.sky.sky_server.dto.CategoryPageQueryDTO;
import com.sky.sky_server.entity.Category;
import com.sky.sky_server.generated.category.model.CategoryCreateRequest;
import com.sky.sky_server.mapper.CategoryMapper;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.service.CategoryService;
import com.sky.sky_server.vo.CategoryPageVO;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    private final CategoryMapper categoryMapper;

    public CategoryServiceImpl(CategoryMapper categoryMapper) {
        this.categoryMapper = categoryMapper;
    }

    @Override
    public void save(CategoryCreateRequest categoryCreateRequest) {
        Category category = new Category();
        category.setName(categoryCreateRequest.getName());
        category.setType(categoryCreateRequest.getType().getValue());
        category.setSort(categoryCreateRequest.getSort() == null ? 0 : categoryCreateRequest.getSort());
        category.setStatus(1);
        category.setCreateTime(LocalDateTime.now());
        category.setUpdateTime(LocalDateTime.now());
        category.setCreateUser(BaseContext.getCurrentId());
        category.setUpdateUser(BaseContext.getCurrentId());

        categoryMapper.insert(category);
    }

    @Override
    public PageResult pageQuery(CategoryPageQueryDTO categoryPageQueryDTO) {
        PageHelper.startPage(categoryPageQueryDTO.getPage(), categoryPageQueryDTO.getPageSize());
        Page<CategoryPageVO> page = (Page<CategoryPageVO>) categoryMapper.pageQuery(categoryPageQueryDTO);
        return new PageResult(page.getTotal(), page.getResult());
    }

    @Override
    public void startOrStop(Integer status, Long id) {
        categoryMapper.updateStatus(status, id, LocalDateTime.now(), BaseContext.getCurrentId());
    }

    @Override
    public List<CategoryPageVO> listEnabledByType(Integer type) {
        return categoryMapper.listEnabledByType(type);
    }
}
