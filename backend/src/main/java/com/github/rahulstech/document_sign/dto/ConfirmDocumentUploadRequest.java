package com.github.rahulstech.document_sign.dto;

import jakarta.validation.constraints.NotBlank;

public record ConfirmDocumentUploadRequest(
        @NotBlank(message = "key not found")
        String key
) {}
