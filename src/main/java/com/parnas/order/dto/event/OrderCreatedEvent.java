package com.parnas.order.dto.event;

import java.math.BigDecimal;
import java.util.UUID;

public record OrderCreatedEvent(
        UUID orderId,
        String customerName,
        BigDecimal totalAmount
) {}