package com.example.ecommerce.config;

import com.example.ecommerce.response.BusinessException;
import com.example.ecommerce.response.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException ex){
        return Result.failure(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(RuntimeException.class)
    public Result<?> handleRuntimeException(RuntimeException ex) {
        log.error("Unhandled RuntimeException occurred:", ex);
        return Result.failure("Internal Server Error!");
    }
}
