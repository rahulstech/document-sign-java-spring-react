package com.github.rahulstech.document_sign.service.upload;

public record CreateUploadUrlParam(
        String contentType,
        Long contentLength
) {}
