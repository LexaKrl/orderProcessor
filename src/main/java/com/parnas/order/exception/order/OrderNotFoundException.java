package com.parnas.order.exception.order;

import com.parnas.order.exception.base.NotFoundException;

import java.util.UUID;

public class OrderNotFoundException extends NotFoundException {
    public OrderNotFoundException(UUID id) {
        super("Order not found with id: %s".formatted(id));
    }
}
