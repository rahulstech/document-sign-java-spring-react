package com.github.rahulstech.document_sign.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DocumentInfo {
    private String url;
    private String mimeType;
    private String name;
    private Document.Status status;
}
