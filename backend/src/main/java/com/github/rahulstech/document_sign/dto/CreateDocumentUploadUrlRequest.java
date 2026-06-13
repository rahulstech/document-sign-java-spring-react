package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.service.upload.CreateUploadUrlParam;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.util.Objects;

public record CreateDocumentUploadUrlRequest(
        String type,

        @Min(value = 1, message = "empty document")
        @Max(
                value = CreateDocumentUploadUrlRequest.MAX_CONTENT_LENGTH,
                message = "max allowed document size 100 mb"
        )
        Long size
) {
    public static final long MAX_CONTENT_LENGTH = 100 * 1024 * 1024; // 100 MB

    @AssertTrue(message = "file type not accepted")
    public boolean isValidContentType() {
        return Objects.equals(type, "application/pdf");
    }

    public CreateUploadUrlParam toCreateUploadUrlParam() {
        return new CreateUploadUrlParam(type, size);
    }
}