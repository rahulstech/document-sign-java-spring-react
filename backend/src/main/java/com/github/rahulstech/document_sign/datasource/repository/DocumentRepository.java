package com.github.rahulstech.document_sign.datasource.repository;

import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.model.DocumentInfo;
import com.github.rahulstech.document_sign.datasource.model.DocumentSignInfo;
import org.jspecify.annotations.NonNull;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<@NonNull Document, @NonNull UUID> {

//    @Modifying
//    @Query("""
//        UPDATE Document d
//        SET
//            d.status = :status,
//            d.lastModified = CURRENT_TIMESTAMP
//        WHERE d.id = :id
//    """)
//    void changeDocumentStatus(@NonNull @Param("id") UUID id, @Param("status") Document.@NonNull Status newStatus);


    @Modifying
    @Query("""
        UPDATE Document d
        SET
            d.status = 'SIGNED',
            d.signedUrl = :signedUrl,
            d.lastModified = CURRENT_TIMESTAMP
        WHERE d.id = :id
    """)
    void markDocumentSigned(@NonNull @Param("id") UUID id, @NonNull @Param("signedUrl") String signedUrl);

    @Query("""
         SELECT d.url, d.mimeType, d.name, d.status
         FROM Document d
         WHERE d.id = :id
    """)
    Optional<DocumentInfo> findDocumentInfoById(@NonNull UUID id);

    @Query("""
         SELECT d.url
         FROM Document d
         WHERE d.id = :id
    """)
    Optional<String> findDocumentUrlById(@NonNull UUID id);

    @Query("""
         SELECT d.status, d.signedUrl
         FROM Document d
         WHERE d.id = :id
    """)
    Optional<DocumentSignInfo> getDocumentSignedInfo(@NonNull UUID id);
}
