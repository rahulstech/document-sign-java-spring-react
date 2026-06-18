package com.github.rahulstech.document_sign.datasource.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class SignatureInfo {

    UUID id;

    UUID documentId;

    String name;

    Member.SignatureData data;
}
