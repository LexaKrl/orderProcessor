package com.parnas.order.controller;

import com.parnas.order.api.OrderApi;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class OrderController implements OrderApi {
    @Override
    public void createOrder(OrderRequest orderRequest) {

    }

    @Override
    public Page<OrderResponse> getOrders(String status, int page, int size, String sort) {
        return null;
    }

    @Override
    public OrderResponse getOrder(UUID id) {
        return null;
    }

    @Override
    public void updateOrderStatus(UUID id, OrderUpdateStatusRequest updateStatusRequest) {

    }
}
