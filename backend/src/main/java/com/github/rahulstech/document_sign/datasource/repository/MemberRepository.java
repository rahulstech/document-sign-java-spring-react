package com.github.rahulstech.document_sign.datasource.repository;

import com.github.rahulstech.document_sign.datasource.model.Member;
import com.github.rahulstech.document_sign.datasource.model.MemberInfo;
import com.github.rahulstech.document_sign.datasource.model.SignatureInfo;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberRepository extends JpaRepository<@NonNull Member, @NonNull UUID> {

    List<Member> findByDocumentId(UUID docId);

    @Query("""
        SELECT m.id, m.documentId, m.name, m.isEmailNotified, m.role, m.action, m.actionedAt, m.clientIP
        FROM Member m
        WHERE m.documentId = :documentId
    """)
    Optional<List<MemberInfo>> findMembersOfDocument(@NonNull @Param("documentId") UUID documentId);


    @Query("""
        SELECT m.id, m.documentId, m.name, m.actionData
        FROM Member m
        WHERE m.documentId = :documentId AND m.role = 'SIGNER'
    """)
    Optional<List<SignatureInfo>> findSignaturesOfDocument(@NonNull @Param("documentId") UUID documentId);
}
