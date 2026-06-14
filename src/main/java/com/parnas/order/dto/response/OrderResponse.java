package com.parnas.order.dto.response;

import com.parnas.order.model.enumuration.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.UUID;

public record OrderResponse(
    UUID id,
    String customerName,
    LocalDateTime orderDate,
    OrderStatus status,
    Page<OrderItemResponse> items
) {
}
