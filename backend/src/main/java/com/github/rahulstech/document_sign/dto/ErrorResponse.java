package com.github.rahulstech.document_sign.dto;

import java.util.List;
import java.util.Map;

public sealed interface ErrorResponse{

    record FieldError(
            Integer code,
            Map<String, String> errors
    ) implements ErrorResponse {}

    record GeneralError(Integer code, List<String> errors) implements ErrorResponse {

        public GeneralError(Integer code, String error) {
                this(code, List.of(error));
            }

    }
}
