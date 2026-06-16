package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.SelfSignRequest;
import com.github.rahulstech.document_sign.service.signature.SignatureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/docs/{doc_id}/signature")
@CrossOrigin("*")
@RequiredArgsConstructor
public class SignatureController {



    private final SignatureService signatureService;


    @PostMapping("/self")
    public ResponseEntity<@NonNull Void> signedByMe(@PathVariable("doc_id") String docId, @Valid @RequestBody SelfSignRequest body) {

        var userId = "guest"; // TODO: get the user id from logged in user

        signatureService.saveSelfSignature(userId, docId, body);

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/signers/{mem_id}")
    public void signedByMember(
            @PathVariable("doc_id") String docId,
            @PathVariable("mem_id") String memId
    ) {


    }
}
