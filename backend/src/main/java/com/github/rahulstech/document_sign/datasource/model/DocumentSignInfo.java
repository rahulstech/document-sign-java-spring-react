package com.github.rahulstech.document_sign.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Projection class containing status and signedUrl of a document.
 */
@Getter
@AllArgsConstructor
public class DocumentSignInfo {
    private Document.Status status;
    private String signedUrl;
}
