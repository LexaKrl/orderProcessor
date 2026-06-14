package com.parnas.order.controller.handler;

import com.parnas.order.dto.exception.BaseExceptionMessage;
import com.parnas.order.dto.exception.ValidationExceptionMessage;
import com.parnas.order.exception.base.ServiceException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<BaseExceptionMessage> handleServiceException(ServiceException e) {
        log.error("ServiceException: {}", e.getMessage(), e);
        BaseExceptionMessage body = BaseExceptionMessage.builder()
                .error(e.getClass().getSimpleName())
                .statusCode(e.getStatus().value())
                .message(e.getMessage())
                .build();
        return ResponseEntity.status(e.getStatus()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationExceptionMessage> handleValidationException(MethodArgumentNotValidException ex) {
        log.warn("Validation failed: {}", ex.getMessage());
        List<ValidationExceptionMessage.Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ValidationExceptionMessage.Violation.builder()
                        .fieldName(error.getField())
                        .message(error.getDefaultMessage())
                        .build())
                .toList();

        ValidationExceptionMessage body = ValidationExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Validation failed")
                .violations(violations)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ValidationExceptionMessage> handleConstraintViolation(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        List<ValidationExceptionMessage.Violation> violations = ex.getConstraintViolations().stream()
                .map(violation -> ValidationExceptionMessage.Violation.builder()
                        .fieldName(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build())
                .toList();

        ValidationExceptionMessage body = ValidationExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message("Constraint violation")
                .violations(violations)
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ValidationExceptionMessage> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("Type mismatch: {}", ex.getMessage());
        ValidationExceptionMessage body = ValidationExceptionMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(ex.getClass().getSimpleName())
                .message("Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ValidationExceptionMessage> handleMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("Message not readable: {}", ex.getMessage());
        ValidationExceptionMessage body = ValidationExceptionMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(ex.getClass().getSimpleName())
                .message("Malformed JSON request")
                .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<BaseExceptionMessage> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("Method not supported: {}", ex.getMessage());
        BaseExceptionMessage body = BaseExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.METHOD_NOT_ALLOWED.value())
                .message(ex.getMessage())
                .build();
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(body);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<BaseExceptionMessage> handleNotFound(NoHandlerFoundException ex) {
        log.warn("Endpoint not found: {}", ex.getRequestURL());
        BaseExceptionMessage body = BaseExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.NOT_FOUND.value())
                .message("Endpoint not found: " + ex.getRequestURL())
                .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<BaseExceptionMessage> handleGenericException(Exception e) {
        log.error("Unexpected error occurred: {}", e.getMessage(), e);

        BaseExceptionMessage body = BaseExceptionMessage.builder()
                .error(e.getClass().getSimpleName())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message("An unexpected internal server error occurred")
                .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }
}