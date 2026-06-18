package com.github.rahulstech.document_sign.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.OffsetDateTime;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class MemberInfo {

    private UUID id;

    private UUID documentId;

    private String name;

    private Boolean isEmailNotified;

    private Member.Role role;

    private Member.Action action;

    private String clientIP;

    private OffsetDateTime actionedAt;
}
