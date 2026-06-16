package com.github.rahulstech.document_sign.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateUploadUrlRequest(
        @NotBlank(message = "type required")
        String type,

        @Min(value = 1, message = "size is too small")
        Long size
) {}
