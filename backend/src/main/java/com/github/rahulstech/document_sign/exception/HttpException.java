package com.github.rahulstech.document_sign.exception;

import org.springframework.http.HttpStatus;

import java.util.Collections;
import java.util.Map;

public class HttpException extends RuntimeException {

    private final HttpStatus httpStatus;

    public HttpException(int status, String message) {
        this(status, message, null);
    }

    public HttpException(int status, String message, Throwable cause) {
        super(message,cause);
        httpStatus = HttpStatus.valueOf(status);
    }

    public static HttpException internalServerError(String message) {
        return  new HttpException(500, message);
    }
}
