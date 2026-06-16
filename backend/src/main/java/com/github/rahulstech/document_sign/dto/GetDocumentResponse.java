package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Document;

import java.time.OffsetDateTime;
import java.util.UUID;

public record GetDocumentResponse(
    UUID id,
    String url,
    String signedUrl,
    String mimeType,
    String name,
    OffsetDateTime createdAt,
    Boolean isPending,
    Boolean isSigned,
    Boolean isVerified
) {

    public static GetDocumentResponse fromDocument(Document doc) {
        var status = doc.getStatus();
        return new GetDocumentResponse(
                doc.getId(),
                doc.getUrl(),
                doc.getSignedUrl(),
                doc.getMimeType(),
                doc.getName(),
                doc.getCreatedAt(),
                status.isPending(),
                status.isSigned(),
                status.isVerified()
        );
    }
}
