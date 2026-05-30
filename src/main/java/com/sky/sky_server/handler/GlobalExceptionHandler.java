package com.sky.sky_server.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.result.Result;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException exception) {
        return Result.error(exception.getMessage());
    }
}
