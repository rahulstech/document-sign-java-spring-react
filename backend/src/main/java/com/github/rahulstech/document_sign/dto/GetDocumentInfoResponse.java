package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.model.DocumentInfo;

import java.util.UUID;

public record GetDocumentInfoResponse(
    String url,
    String mimeType,
    String name,
    Boolean canEdit
) {

    public static GetDocumentInfoResponse fromDocument(DocumentInfo info) {
        var status = info.getStatus();
        return new GetDocumentInfoResponse(
                info.getUrl(),
                info.getMimeType(),
                info.getName(),
                status.isPending()
        );
    }
}
