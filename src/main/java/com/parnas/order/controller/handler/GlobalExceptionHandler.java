package com.parnas.order.controller.handler;

import com.parnas.order.dto.exception.BaseExceptionMessage;
import com.parnas.order.dto.exception.ValidationExceptionMessage;
import com.parnas.order.exception.base.ServiceException;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.ui.Model;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<BaseExceptionMessage> handleServiceException(Model model, ServiceException e) {
        log.info("Entered into ServiceExceptionHandler");
        return ResponseEntity.status(e.getStatus().value()).body(
                BaseExceptionMessage.builder()
                    .error(e.getClass().getSimpleName())
                    .statusCode(e.getStatus().value())
                    .message(e.getMessage())
                    .build());

    }

    @ExceptionHandler(Exception.class)
    public BaseExceptionMessage handleException(Model model, Exception e) {
        log.info("Entered into ExceptionHandler");
        return BaseExceptionMessage.builder()
                .error(e.getClass().getSimpleName())
                .statusCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .message(e.getMessage())
                .build();
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ValidationExceptionMessage handleValidationException(MethodArgumentNotValidException ex) {
        log.info("Entered into ValidationExceptionHandler");
        final List<ValidationExceptionMessage.Violation> violations = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> ValidationExceptionMessage.Violation.builder()
                        .fieldName(error.getField())
                        .message(error.getDefaultMessage())
                        .build()
                )
                .toList();
        return ValidationExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(ex.getStatusCode().value())
                .message(ex.getMessage())
                .violations(violations)
                .build();
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ValidationExceptionMessage handleConstraintViolationException(ConstraintViolationException ex) {
        log.info("Entered into ConstraintViolationExceptionHandler");
        final List<ValidationExceptionMessage.Violation> violations = ex.getConstraintViolations().stream()
                .map(violation -> ValidationExceptionMessage.Violation.builder()
                        .fieldName(violation.getPropertyPath().toString())
                        .message(violation.getMessage())
                        .build()
                )
                .toList();
        return ValidationExceptionMessage.builder()
                .error(ex.getClass().getSimpleName())
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .message(ex.getMessage())
                .violations(violations)
                .build();
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final ValidationExceptionMessage handleMethodArgumentTypeMismatchException(MethodArgumentTypeMismatchException exception) {
        log.info("Entered into MethodArgumentTypeMismatchExceptionHandler");
        return ValidationExceptionMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(exception.getClass().getSimpleName())
                .message(exception.getMessage())
                .build();
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public final ValidationExceptionMessage handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        log.info("Entered into HttpMessageNotReadableExceptionHandler");
        return ValidationExceptionMessage.builder()
                .statusCode(HttpStatus.BAD_REQUEST.value())
                .error(exception.getClass().getSimpleName())
                .message(exception.getMessage())
                .build();
    }
}