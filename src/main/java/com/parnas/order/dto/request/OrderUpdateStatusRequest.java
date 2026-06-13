package com.parnas.order.dto.request;

import com.parnas.order.model.enumuration.OrderStatus;
import jakarta.validation.constraints.NotNull;

public record OrderUpdateStatusRequest(
        @NotNull(message = "The order's status must be not null")
        OrderStatus status
) {
}
