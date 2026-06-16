package com.github.rahulstech.document_sign.datasource.repository;

import com.github.rahulstech.document_sign.datasource.model.Document;
import com.github.rahulstech.document_sign.datasource.model.DocumentInfo;
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

    @Modifying
    @Query("""
        UPDATE Document d
        SET
            d.status = :status,
            d.lastModified = CURRENT_TIMESTAMP
        WHERE d.id = :id
    """)
    void changeDocumentStatus(@NonNull @Param("id") UUID id, @Param("status") Document.@NonNull Status newStatus);

    @Query("""
         SELECT d.url, d.mimeType, d.name, d.status
         FROM Document d
         WHERE d.id = :id
    """)
    Optional<DocumentInfo> findDocumentInfoById(@NonNull @Param("id") UUID id);
}
