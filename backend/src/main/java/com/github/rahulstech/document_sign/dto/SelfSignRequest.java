package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Member;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SelfSignRequest(
        @NotBlank(message = "uploadKey should not empty")
        String uploadKey,

        @Min(value = 1, message = "pageNumber must be greater than zero")
        Integer pageNumber,

        @DecimalMin(value = "0.0", message = "left must not be negative")
        Double left,

        @DecimalMin(value = "0.0", message = "top must not be negative")
        @NotNull
        Double top,

        @DecimalMin(value = "0.0", message = "width must not be negative")
        @NotNull
        Double width,

        @DecimalMin(value = "0.0", message = "height must not be negative")
        Double height
) {

    public Member.SignatureData toSignatureData(String url) {
         return new Member.SignatureData(url, pageNumber, left, top, width, height);
    }
}
