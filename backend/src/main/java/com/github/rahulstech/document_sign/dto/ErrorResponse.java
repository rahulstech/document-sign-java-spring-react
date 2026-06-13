package com.github.rahulstech.document_sign.dto;

import java.util.List;

public record ErrorResponse(
   Integer code,
   Boolean success,
   List<String> errors
) {}
