package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.dto.SelfSignRequest;
import com.github.rahulstech.document_sign.service.signature.SignatureService;
import com.github.rahulstech.document_sign.util.HttpRequestUtil;
import jakarta.servlet.http.HttpServletRequest;
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
    public ResponseEntity<@NonNull Void> signedByMe(@PathVariable("doc_id") String docId, @Valid @RequestBody SelfSignRequest body, HttpServletRequest req) {

        var userId = "guest"; // TODO: get the user id from logged in user
        var clientIP = HttpRequestUtil.getClientIp(req);

        signatureService.saveSelfSignature(userId, docId, body, clientIP);

        return ResponseEntity.status(201).build();
    }

    @PostMapping("/signers/{mem_id}")
    public void signedByMember(
            @PathVariable("doc_id") String docId,
            @PathVariable("mem_id") String memId
    ) {


    }
}
