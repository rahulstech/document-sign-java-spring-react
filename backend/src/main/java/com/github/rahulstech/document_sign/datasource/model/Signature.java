package com.github.rahulstech.document_sign.datasource.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "signatures")
public class Signature {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "sig_id")
    private UUID id;

    @Column(name = "mem_id", nullable = false, updatable = false)
    private UUID memberId;

    @Column(name = "sig_url", nullable = false, updatable = false)
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sig_data", columnDefinition = "jsonb", nullable = false, updatable = false)
    private Data data;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;


    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Data {
        Integer pageNumber;

        Double x;

        Double y;

        Double width;

        Double height;
    }
}
