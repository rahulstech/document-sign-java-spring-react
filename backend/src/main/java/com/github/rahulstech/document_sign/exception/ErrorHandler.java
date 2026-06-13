package com.github.rahulstech.document_sign.exception;

import com.github.rahulstech.document_sign.dto.ErrorResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.util.List;

@ControllerAdvice
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger("ErrorHandler");

    @ExceptionHandler(HttpException.class)
    public @NonNull ResponseEntity<ErrorResponse> handleHttpException(HttpException ex, WebRequest req) {
        var httpState = ex.httpStatus;
        var success = httpState.is2xxSuccessful();
        var res = new ErrorResponse(
                httpState.value(),
                success,
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(httpState).body(res);
    }
}
