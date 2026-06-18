package com.github.rahulstech.document_sign.service.signature;

import com.github.rahulstech.document_sign.datasource.model.Member;
import com.github.rahulstech.document_sign.datasource.repository.DocumentRepository;
import com.github.rahulstech.document_sign.datasource.repository.MemberRepository;
import com.github.rahulstech.document_sign.dto.SelfSignRequest;
import com.github.rahulstech.document_sign.service.pdfservice.PdfData;
import com.github.rahulstech.document_sign.service.pdfservice.PdfService;
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
    private final PdfService pdfService;

    /**
     * Handles saving a self-signature on a document.
     * This involves saving the uploaded signature file to permanent storage,
     * registering the signer member's actions in the database, and updating
     * the document's state to signed with the annotated PDF.
     *
     * @param userId The ID of the signing user.
     * @param documentId The ID of the document being signed.
     * @param req The self-signature request details.
     * @param clientIP The IP address of the client performing the signature.
     */
    @Transactional
    public void saveSelfSignature(String userId, UUID documentId, SelfSignRequest req, String clientIP) {
        // TODO: get display name and email of the logged in user
        var random = new Random();
        var randomLong = random.nextLong(1, 10000000);
        var userDisplayName = "John " + randomLong;
        var userEmail = String.format("john%d@domain.com", randomLong);

        // save the signature
        var result = storageService.saveUpload(req.uploadKey(), userId, documentId.toString());

        // insert member
        var actionData = req.toSignatureData(result.publicUrl());
        var savedMember = insertSelfSignature(documentId, userDisplayName, userEmail, actionData, clientIP);

        // TODO: add audit log

        updateDocumentStatus(savedMember);
    }

    /**
     * Inserts a record for the signer member indicating they have completed their signature.
     *
     * @param documentId The ID of the associated document.
     * @param memberName The name of the member.
     * @param memberEmail The email of the member.
     * @param actionData The signature coordinates and S3 URL data.
     * @param clientIP The client's IP address.
     * @return The saved Member entity.
     */
    private Member insertSelfSignature(
            UUID documentId,
            String memberName,
            String memberEmail,
            Member.SignatureData actionData,
            String clientIP
    ) {
        // create member
        var member = new Member();
        member.setName(memberName);
        member.setEmail(memberEmail);
        member.setDocumentId(documentId);
        member.setRole(Member.Role.SIGNER);
        member.setAction(Member.Action.SIGNED);
        member.setActionedAt(OffsetDateTime.now());
        member.setActionData(actionData);
        member.setClientIP(clientIP);

        // save member and return
        return memRepo.saveAndFlush(member);
    }

    /**
     * Updates the status of the document to SIGNED by overlaying the member's signature
     * on the PDF and updating the document S3 URL and status in the repository.
     *
     * @param savedMember The member who signed the document.
     */
    private void updateDocumentStatus(Member savedMember) {
        try {
            var documentId = savedMember.getDocumentId();

            // create signature annotated pdf
            var documentUrl = docRepo.findDocumentUrlById(documentId).orElseThrow();
            var pdfData = new PdfData(documentUrl, PdfData.Signature.fromSignerMember(savedMember));
            var signedUrl = pdfService.createSignatureAnnotatedPdf(pdfData);

            // update document status to SIGNED
            docRepo.markDocumentSigned(documentId, signedUrl);
        } catch (Exception e) {
            throw new RuntimeException("Failed to update document status with signature annotated PDF", e);
        }
    }
}
