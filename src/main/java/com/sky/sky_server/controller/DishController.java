package com.sky.sky_server.controller;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sky.sky_server.dto.DishDTO;
import com.sky.sky_server.dto.DishPageQueryDTO;
import com.sky.sky_server.result.PageResult;
import com.sky.sky_server.result.Result;
import com.sky.sky_server.service.DishService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

@RestController
@RequestMapping("/admin/dish")
@Validated
public class DishController {
    private final DishService dishService;

    public DishController(DishService dishService) {
        this.dishService = dishService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Result<Long> save(@Valid @RequestBody DishDTO dishDTO) {
        return Result.success(dishService.save(dishDTO));
    }

    @GetMapping("/page")
    public Result<PageResult> page(@Valid DishPageQueryDTO dishPageQueryDTO) {
        return Result.success(dishService.pageQuery(dishPageQueryDTO));
    }

    @PostMapping("/status/{status}")
    public Result<String> startOrStop(@PathVariable @Min(0) @Max(1) Integer status,
            @RequestParam @Positive Long id) {
        dishService.startOrStop(status, id);
        return Result.success("update dish status success");
    }
}
