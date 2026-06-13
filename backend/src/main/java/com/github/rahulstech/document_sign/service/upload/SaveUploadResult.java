package com.github.rahulstech.document_sign.service.upload;

public record SaveUploadResult(
        String publicUrl,
        String contentType,
        Long contentLength
) {}
