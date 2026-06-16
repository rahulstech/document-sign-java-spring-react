package com.github.rahulstech.document_sign.service.storageservice;

public record SaveUploadResult(
        String publicUrl,
        String contentType,
        Long contentLength
) {}
