package com.example.ecommerce.response;

public class BusinessException extends RuntimeException{
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public BusinessException(String message) {
        this(400,message);
    }

    public Integer getCode() {
        return code;
    }
}
