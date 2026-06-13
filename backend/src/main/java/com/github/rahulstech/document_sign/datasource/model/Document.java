package com.github.rahulstech.document_sign.datasource.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;


@Getter
@Setter
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doc_id")
    private UUID id;

    @Transient // TODO: remove this annotation when auth implemented
    private String userId;

    @Column(name = "doc_url", nullable = false)
    private String url;

    @Column(name = "doc_type", nullable = false)
    private String type;

    @Column(name = "doc_name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "doc_status", nullable = false)
    private Status status = Status.CREATED;

    @CreationTimestamp
    @Column(name = "created_at",  nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;

    public enum Status {
        CREATED,

        DRAFT,

        PUBLISHED,

        SIGNED_WITHOUT_VERIFY,

        SINGED_AND_VERIFIED,

        ;

        public boolean isPublished() {
            return ordinal() >= PUBLISHED.ordinal();
        }

        public boolean isSigned() {
            return SIGNED_WITHOUT_VERIFY == this || SINGED_AND_VERIFIED == this;
        }

        public boolean isVerified() {
            return SINGED_AND_VERIFIED == this;
        }
    }
}
