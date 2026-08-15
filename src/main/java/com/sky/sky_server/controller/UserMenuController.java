package com.sky.sky_server.controller;

import java.util.List;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.CategoryService;
import com.sky.sky_server.service.DishService;
import com.sky.sky_server.vo.CategoryPageVO;
import com.sky.sky_server.vo.DishCustomerVO;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/user")
@Validated
public class UserMenuController {
    private final CategoryService categoryService;
    private final DishService dishService;

    public UserMenuController(CategoryService categoryService, DishService dishService) {
        this.categoryService = categoryService;
        this.dishService = dishService;
    }

    @GetMapping("/category/list")
    public Result<List<CategoryPageVO>> categories(@RequestParam(defaultValue = "1") @Min(1) @Max(2) Integer type) {
        return Result.success(categoryService.listEnabledByType(type));
    }

    @GetMapping("/dish/list")
    public Result<List<DishCustomerVO>> dishes(@RequestParam @Positive Long categoryId) {
        return Result.success(dishService.listEnabledByCategoryId(categoryId));
    }
}
