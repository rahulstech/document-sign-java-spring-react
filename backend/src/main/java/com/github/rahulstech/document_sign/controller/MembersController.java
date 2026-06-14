package com.github.rahulstech.document_sign.controller;

import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.datasource.repository.MemberRepository;
import com.github.rahulstech.document_sign.dto.AddMemberRequest;
import com.github.rahulstech.document_sign.exception.HttpException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<@NonNull Void> addMembers(@PathVariable("doc_id") String docId, @Valid @RequestBody AddMemberRequest body){
        var documentId = UUID.fromString(docId);
        var members = body.toMembers(documentId);

        if (!docRepo.existsById(documentId)) {
            throw HttpException.notFound("document not found");
        }

        memRepo.saveAllAndFlush(members);

        return ResponseEntity.status(201).build();
    }
}
