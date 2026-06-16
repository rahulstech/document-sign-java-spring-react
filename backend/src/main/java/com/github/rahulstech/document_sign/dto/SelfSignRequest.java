package com.github.rahulstech.document_sign.dto;

import com.github.rahulstech.document_sign.datasource.model.Signature;
import jakarta.validation.constraints.NotBlank;

public record SelfSignRequest(
        @NotBlank(message = "")
        String uploadKey,
        Integer pageNumber,
        Double x,
        Double y,
        Double width,
        Double height
) {

    public Signature.Data toData() {
         return new Signature.Data(pageNumber, x, y, width, height);
    }
}
