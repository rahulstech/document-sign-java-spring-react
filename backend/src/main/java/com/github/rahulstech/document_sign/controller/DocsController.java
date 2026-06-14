package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.*;
import com.github.rahulstech.document_sign.exception.HttpException;
import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.service.upload.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DocsController {

    private final UploadService uploadService;

    private final DocumentRepository docRepo;


    @PostMapping("/upload/url")
    public ResponseEntity<@NonNull CreateDocumentUploadUrlResponse> createDocumentUploadUrl(@Valid @RequestBody CreateDocumentUploadUrlRequest req) {
        var result = uploadService.createUploadUrl(req.toCreateUploadUrlParam());
        var res = CreateDocumentUploadUrlResponse.fromCreateUploadUrlResult(result);
        return ResponseEntity.status(201).body(res);
    }

    @PostMapping("/upload/confirm")
    public ConfirmDocumentUploadResponse confirmDocumentUpload(@Valid @RequestBody ConfirmDocumentUploadRequest req) {
        var userId = "guest"; // TODO: get user id when auth implemented

        // save document in public storage
        var result = uploadService.saveUpload(userId, req.key());

        // create document
        var document = new Document();
        document.setUserId(userId);
        document.setUrl(result.publicUrl());
        document.setType(result.contentType());
        document.setName(req.name());

        // save document in database
        var savedDocument = docRepo.save(document);

        return new ConfirmDocumentUploadResponse(
                savedDocument.getId().toString(), result.publicUrl(), result.contentType(), savedDocument.getName()
        );
    }

    @GetMapping("/{doc_id}")
    public GetDocumentResponse getDocument(@PathVariable("doc_id") UUID docId) {
        var document = docRepo.findById(docId)
                .orElseThrow(() -> HttpException.notFound("no document found for id "+docId));

        return GetDocumentResponse.fromDocument(document);
    }
}
