package com.github.rahulstech.document_sign.exception;

import org.springframework.http.HttpStatus;

public class HttpException extends RuntimeException {

    final HttpStatus httpStatus;

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

    public static HttpException notFound(String message) {
        return new HttpException(404, message);
    }
}
