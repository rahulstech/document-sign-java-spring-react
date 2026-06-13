package com.github.rahulstech.document_sign.datasource.repository;

import com.github.rahulstech.document_sign.datasource.model.Document;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DocumentRepository extends CrudRepository<Document, UUID> {
}
