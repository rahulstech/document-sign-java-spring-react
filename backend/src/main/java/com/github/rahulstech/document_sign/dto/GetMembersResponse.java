package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Member;
import com.github.rahulstech.document_sign.datasource.model.MemberInfo;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public record GetMembersResponse(
        List<MemberData> members
) {

    public static GetMembersResponse fromMemberInfo(List<MemberInfo> infos) {
        var data = infos.stream().map(info -> {
            var action = info.getAction();
            return new MemberData(
                    info.getId(),
                    info.getDocumentId(),
                    info.getName(),
                    info.getIsEmailNotified(),
                    Status.fromAction(action),
                    info.getClientIP(),
                    info.getActionedAt()
            );
        }).toList();
        return new GetMembersResponse(data);
    }

    public record MemberData(
            UUID id,
            UUID documentId,
            String displayName,
            Boolean isNotifiedViaEmail,
            Status actionStatus,
            String actionFromIP,
            OffsetDateTime actionAt
    ) {}

    public enum Status {
        PENDING,

        COMPLETE,

        DECLINED,

        ;

        public boolean isPending() { return this == PENDING; }

        public boolean isComplete() { return this == COMPLETE; }

        public boolean isDeclined() { return this == DECLINED; }

        static Status fromAction(Member.Action action) {
            return switch (action) {
                case Member.Action.PENDING -> PENDING;
                case Member.Action.SIGNED, Member.Action.WITNESSED, Member.Action.VERIFIED -> COMPLETE;
                case Member.Action.DECLINED -> DECLINED;
            };
        }
    }
}
