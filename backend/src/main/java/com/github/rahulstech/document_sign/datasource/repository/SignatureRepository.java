package com.github.rahulstech.document_sign.datasource.repository;

import com.github.rahulstech.document_sign.datasource.model.Signature;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SignatureRepository extends JpaRepository<@NonNull Signature, @NonNull UUID> {
}
