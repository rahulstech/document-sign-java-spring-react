package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.datasource.repository.MemberRepository;
import com.github.rahulstech.document_sign.dto.AddMemberRequest;
import com.github.rahulstech.document_sign.dto.GetMembersResponse;
import com.github.rahulstech.document_sign.exception.HttpException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.UUID;


@RestController
@RequestMapping("/api/docs/{doc_id}/members")
@CrossOrigin("*")
@RequiredArgsConstructor
public class MembersController {

    private static final Logger logger = LoggerFactory.getLogger(MembersController.class);

    private final MemberRepository memRepo;
    private final DocumentRepository docRepo;

    @PostMapping
    public ResponseEntity<@NonNull Void> addMembers(@PathVariable("doc_id") UUID documentId, @Valid @RequestBody AddMemberRequest body){
        var members = body.toMembers(documentId);

        checkDocumentExistsOrThrow(documentId);

        memRepo.saveAllAndFlush(members);

        return ResponseEntity.status(201).build();
    }

    @GetMapping("/")
    public GetMembersResponse getMembersOfDocument(@PathVariable("doc_id") UUID documentId){
        checkDocumentExistsOrThrow(documentId);

        var infos = memRepo.findMembersOfDocument(documentId).orElse(Collections.emptyList());

        return GetMembersResponse.fromMemberInfo(infos);
    }

    private void checkDocumentExistsOrThrow(UUID documentId) {
        if (!docRepo.existsById(documentId)) {
            logger.info("getMembersOfDocument did not find document with id {}", documentId);
            throw HttpException.notFound("document not found");
        }
    }
}
