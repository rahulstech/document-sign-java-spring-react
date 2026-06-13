package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Document;

import java.util.UUID;

public record GetDocumentResponse(
    UUID id,
    String url,
    String type,
    String name,
    Boolean isPublished,
    Boolean isSigned,
    Boolean isVerified
) {

    public static GetDocumentResponse fromDocument(Document doc) {
        var status = doc.getStatus();
        return new GetDocumentResponse(
                doc.getId(),
                doc.getUrl(),
                doc.getType(),
                doc.getName(),
                status.isPublished(),
                status.isSigned(),
                status.isVerified()
        );
    }
}
