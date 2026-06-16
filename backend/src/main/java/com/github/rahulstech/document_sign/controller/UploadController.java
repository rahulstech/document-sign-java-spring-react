package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.CreateUploadUrlRequest;
import com.github.rahulstech.document_sign.dto.CreateUploadUrlResponse;
import com.github.rahulstech.document_sign.service.upload.UploadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin("*")
@RequiredArgsConstructor
public class UploadController {

    private final UploadService uploadService;

    @PostMapping("/upload")
    public CreateUploadUrlResponse createUploadUrl(@Valid @RequestBody CreateUploadUrlRequest body) {
        return uploadService.createUploadUrl(body);
    }
}
