package com.parnas.order.api;

import com.parnas.order.dto.exception.BaseExceptionMessage;
import com.parnas.order.dto.exception.ValidationExceptionMessage;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.model.enumuration.OrderStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Validated
@Tag(name = "Order", description = "the Order Api")
@RequestMapping("/api/orders")
public interface OrderApi {

    @Operation(summary = "Create new Order", description = "This operation creates a new Order")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201",
                    description = "Order successfully created"),

            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = ValidationExceptionMessage.class))),
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void createOrder(
            @Parameter(description = "Create a new order in the store", required = true)
            @Valid @RequestBody OrderRequest orderRequest
    );

    @Operation(summary = "Get orders", description = "This operation returns a page of orders")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Order have been found successfully",
                    content = @Content(schema = @Schema(implementation = Page.class))),

            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = ValidationExceptionMessage.class))),
    })
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    Page<OrderResponse> getOrders(
        @Parameter(description = "Filter by order status", in = ParameterIn.QUERY)
        @RequestParam(required = false) OrderStatus status,

        @Parameter(description = "The number of page (starts with 0)", in = ParameterIn.QUERY)
        @RequestParam(defaultValue = "0") int page,

        @Parameter(description = "The page size", in = ParameterIn.QUERY)
        @RequestParam(defaultValue = "10") int size,

        @Parameter(description = "Sort by order field", in = ParameterIn.QUERY)
        @RequestParam(required = false) String sort
    );

    @Operation(summary = "Get order by id", description = "This operation returns an order by its id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",
                    description = "Order have been found successfully",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = OrderResponse.class))),

            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = ValidationExceptionMessage.class))),

            @ApiResponse(responseCode = "404",
                    description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = BaseExceptionMessage.class))),
    })
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    OrderResponse getOrder(
            @Parameter(description = "The order's UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,

            @Parameter(description = "The order's items page number",  in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "0") int orderItemsPage,

            @Parameter(description = "The order's items page size", in = ParameterIn.QUERY)
            @RequestParam(defaultValue = "10") int  orderItemsSize
    );

    @Operation(summary = "Change the order's status", description = "This operation changes order's status")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204",
                    description = "Order's status has been updated successfully"),

            @ApiResponse(responseCode = "400",
                    description = "Bad Request",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = ValidationExceptionMessage.class))),

            @ApiResponse(responseCode = "404",
                    description = "Not Found",
                    content = @Content(mediaType = "application/json",
                            schema =  @Schema(implementation = BaseExceptionMessage.class))),
    })
    @PutMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void updateOrderStatus(
            @Parameter(description = "The order's UUID", required = true, example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable UUID id,

            @Parameter(description = "The status order to be updated", required = true)
            @Valid @RequestBody OrderUpdateStatusRequest updateStatusRequest
    );
}
