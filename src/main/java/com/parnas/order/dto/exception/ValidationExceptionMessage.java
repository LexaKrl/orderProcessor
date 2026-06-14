package com.parnas.order.dto.exception;

import lombok.Builder;
import lombok.Getter;
import lombok.experimental.SuperBuilder;

import java.util.List;

@SuperBuilder
@Getter
public class ValidationExceptionMessage extends AbstractExceptionMessage {
    List<Violation> violations;

    @Builder
    public record Violation(String fieldName, String message) {}
}