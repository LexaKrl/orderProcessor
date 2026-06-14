package com.parnas.order.exception.base;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpStatus;

@Getter
@Setter
public class ServiceException extends RuntimeException {

    private final HttpStatus status;

    public ServiceException(final String message, final HttpStatus status) {
        super(message);
        this.status = status;
    }
}
