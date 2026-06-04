package com.sky.sky_server.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.sky.sky_server.exception.BusinessException;
import com.sky.sky_server.result.Result;

import java.sql.SQLIntegrityConstraintViolationException;

import com.sky.sky_server.constant.MessageConstant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public Result<String> handleBusinessException(BusinessException exception) {
        return Result.error(exception.getMessage());
    }

    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    public Result<String> handleSQLIntegrityConstraintViolationException(
            SQLIntegrityConstraintViolationException exception) {
        String message = exception.getMessage();

        if (message != null && message.contains("Duplicate entry")) {
            return Result.error(MessageConstant.ALREADY_EXISTS);
        }

        return Result.error("database error");
    }
}
