package com.parnas.order.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderItemResponse(
        Long id,
        String productName,
        Integer quantity,
        BigDecimal price,
        UUID orderId
) {
}
