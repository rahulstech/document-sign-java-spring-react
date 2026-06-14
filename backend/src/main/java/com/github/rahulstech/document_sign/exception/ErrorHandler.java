package com.github.rahulstech.document_sign.exception;

import com.github.rahulstech.document_sign.dto.ErrorResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ErrorHandler {

    private static final Logger logger = LoggerFactory.getLogger("ErrorHandler");

    @ExceptionHandler(HttpException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleHttpException(HttpException ex) {
        var httpState = ex.httpStatus;
        var res = new ErrorResponse.GeneralError(
                httpState.value(),
                List.of(ex.getMessage())
        );

        return ResponseEntity.status(httpState).body(res);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleInvalidMethodArgument(MethodArgumentNotValidException ex) {
        var errors = ex.getBindingResult().getFieldErrors().stream()
                .collect(
                        Collectors.toMap(
                                FieldError::getField,
                                err -> null == err.getDefaultMessage() ? "invalid value" : err.getDefaultMessage()
                        )
                );
        var res = new ErrorResponse.FieldError(400, errors);

        return ResponseEntity.badRequest().body(res);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<@NonNull ErrorResponse> handleDatabaseError(DataAccessException ex) {
        logger.error("database error", ex);
        var res = new ErrorResponse.GeneralError(500, "server error");
        return ResponseEntity.internalServerError().body(res);
    }

    @ExceptionHandler(Throwable.class)
    public ResponseEntity<@NonNull ErrorResponse> handleDatabaseError(Throwable th) {
        logger.error("unexpected error", th);
        var res = new ErrorResponse.GeneralError(500, "server error");
        return ResponseEntity.internalServerError().body(res);
    }
}
