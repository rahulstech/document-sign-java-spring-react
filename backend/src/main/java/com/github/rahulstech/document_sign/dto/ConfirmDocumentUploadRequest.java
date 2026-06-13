package com.github.rahulstech.document_sign.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfirmDocumentUploadRequest(
        @NotBlank(message = "key not found")
        String key,

        @NotBlank(message = "name not found")
        @Size(max = 255, message = "name must be with in 255 characters")
        String name
) {}
