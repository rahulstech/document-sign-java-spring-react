package com.github.rahulstech.document_sign.dto;

public record ConfirmDocumentUploadResponse(
        String docId,
        String docUrl,
        String docType,
        String docName
) {}
