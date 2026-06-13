package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.ConfirmDocumentUploadRequest;
import com.github.rahulstech.document_sign.dto.ConfirmDocumentUploadResponse;
import com.github.rahulstech.document_sign.dto.CreateDocumentUploadUrlRequest;
import com.github.rahulstech.document_sign.dto.CreateDocumentUploadUrlResponse;
import com.github.rahulstech.document_sign.service.upload.UploadService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
public class DocsController {

    @NonNull
    private UploadService uploadService;

    @PostMapping("/upload/url")
    public CreateDocumentUploadUrlResponse createDocumentUploadUrl(@Valid @RequestBody CreateDocumentUploadUrlRequest req) {
        var result = uploadService.createUploadUrl(req.toCreateUploadUrlParam());
        return CreateDocumentUploadUrlResponse.fromCreateUploadUrlResult(result);
    }

    @PostMapping("/upload/confirm")
    public ConfirmDocumentUploadResponse confirmDocumentUpload(@Valid @RequestBody ConfirmDocumentUploadRequest req) {

        // save the document
        var result = uploadService.saveUpload("guest", req.key()); // TODO: set userId from auth as first parameter

        // add in database

        return new ConfirmDocumentUploadResponse("doc1", result.publicUrl(), result.contentType());
    }
}
