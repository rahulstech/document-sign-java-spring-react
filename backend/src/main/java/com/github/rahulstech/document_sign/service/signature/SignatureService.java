package com.github.rahulstech.document_sign.service.signature;

import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.model.Member;
import com.github.rahulstech.document_sign.datasource.model.Signature;
import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.datasource.repository.MemberRepository;
import com.github.rahulstech.document_sign.datasource.repository.SignatureRepository;
import com.github.rahulstech.document_sign.dto.SelfSignRequest;
import com.github.rahulstech.document_sign.service.storageservice.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Random;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SignatureService {

    private final StorageService storageService;

    private final DocumentRepository docRepo;

    private final MemberRepository memRepo;

    private final SignatureRepository sigRepo;

    @Transactional
    public void saveSelfSignature(String userId, String docId, SelfSignRequest req, String clientIP) {

        Random random = new Random();
        // TODO: get display name and email of the logged in user
        var userDisplayName = "John Doe";
        var userEmail = String.format("johndoe%d@domain.com", random.nextLong(1, 10000000));
        var documentId = UUID.fromString(docId);

        // save the signature
        var result = storageService.saveUpload(req.uploadKey(), userId, docId);

        // add member
        var member = new Member();
        member.setName(userDisplayName);
        member.setEmail(userEmail);
        member.setDocumentId(documentId);
        member.setRole(Member.Role.SIGNER);
        member.setAction(Member.Action.SIGNED);
        member.setActionedAt(OffsetDateTime.now());

        var savedMember = memRepo.saveAndFlush(member);

        // save signature
        var sigData = req.toData();

        var signature = new Signature();
        signature.setMemberId(savedMember.getId());
        signature.setUrl(result.publicUrl());
        signature.setData(sigData);
        signature.setClientIP(clientIP);

        sigRepo.saveAndFlush(signature);

        // update document
        docRepo.changeDocumentStatus(documentId, Document.Status.SIGNED);

        // TODO: add audit log

        // TODO: create annotated document with signature and signature id
    }
}
