package com.sky.sky_server.handler;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

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

    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<String> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        return Result.error(MessageConstant.ALREADY_EXISTS);
    }

    @ExceptionHandler({ MethodArgumentNotValidException.class, HandlerMethodValidationException.class })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<String> handleValidationException(Exception exception) {
        if (exception instanceof MethodArgumentNotValidException validationException
                && validationException.getBindingResult().getFieldError() != null) {
            return Result.error(validationException.getBindingResult().getFieldError().getDefaultMessage());
        }
        return Result.error("request parameter is invalid");
    }
}
