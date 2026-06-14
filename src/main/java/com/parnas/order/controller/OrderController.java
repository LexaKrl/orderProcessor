package com.parnas.order.controller;

import com.parnas.order.api.OrderApi;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.model.enumuration.OrderStatus;
import com.parnas.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @Override
    public void createOrder(OrderRequest orderRequest) {
        orderService.saveOrder(orderRequest);
    }

    @Override
    public Page<OrderResponse> getOrders(OrderStatus status, int page, int size, String sort) {
        return orderService.getOrdersByStatus(status, sort, page, size);
    }

    @Override
    public OrderResponse getOrder(UUID id, int orderItemsPage, int  orderItemsSize) {
        return orderService.getOrderByIdPageable(id, orderItemsPage, orderItemsSize);
    }

    @Override
    public void updateOrderStatus(UUID id, OrderUpdateStatusRequest updateStatusRequest) {

    }
}
