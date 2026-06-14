package com.parnas.order.service;

import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.model.enumuration.OrderStatus;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface OrderService {
    void saveOrder(OrderRequest orderRequest);

    Page<OrderResponse> getOrdersByStatus(OrderStatus status, String sort, int page, int size);

    OrderResponse getOrderByIdPageable(UUID id, int orderItemsPage, int orderItemsSize);

    void updateOrderStatus(UUID id, OrderUpdateStatusRequest updateStatusRequest);
}
