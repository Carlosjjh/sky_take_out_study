package com.sky.sky_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.validation.annotation.Validated;

import com.sky.sky_server.generated.category.model.CategoryCreateRequest;
import com.sky.sky_server.dto.CategoryPageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.CategoryService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import org.springframework.web.bind.annotation.ResponseStatus;

@RestController
@RequestMapping("/admin/category")
@Validated
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<String> save(@Valid @RequestBody CategoryCreateRequest categoryCreateRequest) {
        categoryService.save(categoryCreateRequest);
        return Result.success("save category success");
    }

    @GetMapping("/page")
    public Result<PageResult> page(@Valid CategoryPageQueryDTO categoryPageQueryDTO) {
        return Result.success(categoryService.pageQuery(categoryPageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id) {
        categoryService.startOrStop(status, id);
        return Result.success("update category status success");
    }
}
