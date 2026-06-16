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
@Table(
        name = "members",
        uniqueConstraints = @UniqueConstraint(
                name = "idx_members_doc_id_email",
                columnNames = {"doc_id", "email"}
        )
)
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "mem_id")
    private UUID id;

    @Column(name = "doc_id", nullable = false, updatable = false)
    private UUID documentId;

    @Column(name = "display_name",  nullable = false)
    private String name;

    @Column(name = "email", nullable = false, updatable = false)
    private String email;

    @Column(name = "is_email_notified", nullable = false)
    private Boolean isEmailNotified = false;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false)
    private Action action = Action.PENDING;

    @Column(name = "action_at")
    private OffsetDateTime actionedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false,  updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "last_modified", nullable = false)
    private OffsetDateTime lastModified;




    public enum Role {
        SIGNER,

        WITNESS,

        VERIFIER,
    }

    public enum Action {
        PENDING,

        SIGNED,

        WITNESSED,

        VERIFIED,

        DECLINED,

        ;

        public boolean isPending() { return this == PENDING; }

        public boolean isDone() { return ordinal() > PENDING.ordinal(); }

        public boolean isDeclined() { return this == DECLINED; }
    }
}
