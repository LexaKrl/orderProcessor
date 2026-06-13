package com.parnas.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

@Schema(description = "OrderItem details")
public record OrderItemRequest(
        @Schema(description = "Product name", example = "Lada")
        @NotBlank(message = "Product name cannot be blank")
        @Size(max = 200, message = "Product name cannot exceed 200 characters")
        String productName,

        @Schema(description = "Quantity", example = "2")
        @NotNull(message = "Quantity cannot be null")
        @Min(value = 1, message = "Quantity must be at least 1")
        Integer quantity,

        @Schema(description = "Price per unit", example = "999.99")
        @NotNull(message = "Price cannot be null")
        @DecimalMin(value = "0.01", message = "Price must be greater than 0")
        BigDecimal price
) {
}
