package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.service.upload.CreateUploadUrlResult;

public record CreateDocumentUploadUrlResponse(
        String url,
        String key
) {

    public static CreateDocumentUploadUrlResponse fromCreateUploadUrlResult(CreateUploadUrlResult result) {
        return new CreateDocumentUploadUrlResponse(result.url(), result.key());
    }
}
