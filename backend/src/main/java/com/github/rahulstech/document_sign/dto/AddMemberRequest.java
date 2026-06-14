package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Member;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record AddMemberRequest(
        @Size(min = 1, message = "add at least 1 member")
        @Valid
        List<AddMemberData> members
) {

    @AssertTrue(message = "duplicate email used")
    public boolean isUniqueEmails() {
        // TODO: a better message to show which emails are duplicate
        var emails = members.stream().map(AddMemberData::email).collect(Collectors.toSet());
        return emails.size() == members.size();
    }


    public Iterable<Member> toMembers(UUID documentId) {
        return members.stream().map(data -> {
            var member = new Member();
            member.setDocumentId(documentId);
            member.setName(data.name);
            member.setEmail(data.email);
            member.setRole(Member.Role.valueOf(data.role.toUpperCase()));
            return member;
        }).toList();
    }

    public record AddMemberData(
            @NotBlank(message = "name is required")
            @Size(max = 100, message = "name must be within 100 characters")
            String name,
            @NotBlank(message = "email is required")
            @Email(message = "not a valid email")
            String email,
            @NotBlank(message = "role is required")
            String role
    ){

        @AssertTrue(message = "invalid role")
        public boolean isValidRole() {
            try {
                Member.Role.valueOf(role.toUpperCase());
                return true;
            } catch (Exception e) {
                return false;
            }
        }
    }
}
