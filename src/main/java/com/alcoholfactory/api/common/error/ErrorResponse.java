package com.alcoholfactory.api.common.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record ErrorResponse(
    Instant timestamp,
    int status,
    String error,
    String message,
    String path,
    List<FieldViolation> fieldErrors) {
  public static ErrorResponse of(int status, String error, String message, String path) {
    return new ErrorResponse(Instant.now(), status, error, message, path, List.of());
  }

  public record FieldViolation(String field, String message) {}
}
