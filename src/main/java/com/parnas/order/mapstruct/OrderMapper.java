package com.parnas.order.mapstruct;

import com.parnas.order.dto.request.OrderItemRequest;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.response.OrderItemResponse;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.model.entity.Order;
import com.parnas.order.model.entity.OrderItem;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    @Mapping(target = "orderId", source = "order.id")
    OrderItemResponse toOrderItemResponse(OrderItem orderItem);

    @Mapping(target = "items", expression = "java(orderItemPage.map(this::toOrderItemResponse))")
    OrderResponse toResponse(Order order, Page<OrderItem> orderItemPage);

    @Mapping(target = "id", ignore = true)
    Order toEntity(OrderRequest orderRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "order", expression = "java(order)")
    OrderItem toOrderItem(OrderItemRequest request, @Context Order order);

    List<OrderItem> toOrderItemList(List<OrderItemRequest> items, @Context Order order);
}

