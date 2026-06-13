package com.parnas.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

@Schema(description = "Create new Order request")
public record OrderRequest(
        @Schema(description = "Customer name", example = "Lexa Rt")
        @NotBlank(message = "Customer name cannot be blank")
        @Size(min = 2, max = 50, message = "Customer name must be between 2 and 50 characters")
        String customerName,

        @Schema(description = "List of order items")
        @NotEmpty(message = "Order must contain at least one item")
        @Valid
        List<OrderItemRequest> items
) {
}
