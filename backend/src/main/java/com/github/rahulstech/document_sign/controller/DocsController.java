package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.*;
import com.github.rahulstech.document_sign.exception.HttpException;
import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.model.DocumentSignInfo;
import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.service.storageservice.StorageService;
import jakarta.validation.Valid;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/docs")
@RequiredArgsConstructor
@CrossOrigin("*")
public class DocsController {

    private final StorageService storageService;

    private final DocumentRepository docRepo;

    @PostMapping("/new")
    public ConfirmDocumentUploadResponse confirmDocumentUpload(@Valid @RequestBody ConfirmDocumentUploadRequest body) {
        var userId = "guest"; // TODO: get user id when auth implemented

        // save document in public storage
        var result = storageService.saveUpload(body.key(), userId);

        // create document
        var document = new Document();
        document.setUserId(userId);
        document.setUrl(result.publicUrl());
        document.setMimeType(result.contentType());
        document.setName(body.name());

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

    @GetMapping("/{doc_id}/info")
    public GetDocumentInfoResponse getDocumentInfo(@PathVariable("doc_id") UUID docId) {
        var info = docRepo.findDocumentInfoById(docId)
                .orElseThrow(() -> HttpException.notFound("no document found for id "+docId));

        return GetDocumentInfoResponse.fromDocument(info);
    }

    @GetMapping("/{doc_id}/download-signed")
    public ResponseEntity<@NonNull Void> downloadSignedDocument(@PathVariable("doc_id") UUID docId) {
        var signInfo = docRepo.getDocumentSignedInfo(docId)
                .orElseThrow(() -> HttpException.notFound("no document found for id " + docId));

        if (signInfo.getStatus() != Document.Status.SIGNED || signInfo.getSignedUrl() == null || signInfo.getSignedUrl().isBlank()) {
            throw new HttpException(400, "Document is not signed yet");
        }

        String key = storageService.getS3KeyFromUrl(signInfo.getSignedUrl());
        String presignedUrl = storageService.getTemporaryDownloadUrl(key);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(presignedUrl))
                .build();
    }
}
