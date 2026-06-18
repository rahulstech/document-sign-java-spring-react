package com.github.rahulstech.document_sign.service.pdfservice;

import com.github.rahulstech.document_sign.datasource.model.Member;
import com.github.rahulstech.document_sign.datasource.model.SignatureInfo;

import java.util.List;
import java.util.UUID;

public record PdfData(
        String documentUrl,
        List<Signature> signatures
) {

    public PdfData(String documentUrl, Signature signature) {
        this(documentUrl, List.of(signature));
    }

    public record Signature(
            UUID id,
            String url,
            Integer pageNumber,
            Double left,
            Double top,
            Double width,
            Double height
    ) {

        public static Signature fromSignatureInfo(SignatureInfo info) {
            var data = info.getData();
            return new Signature(
                    info.getId(),
                    data.url(),
                    data.pageNumber(),
                    data.left(),
                    data.top(),
                    data.width(),
                    data.height()
            );
        }

        public static Signature fromSignerMember(Member member) {
            if (member.getRole() != Member.Role.SIGNER) {
                throw new IllegalArgumentException("Member is not signer");
            }
            var data = (Member.SignatureData) member.getActionData();
            return new Signature(
                    member.getId(),
                    data.url(),
                    data.pageNumber(),
                    data.left(),
                    data.top(),
                    data.width(),
                    data.height()
            );
        }
    }
}
