package com.parnas.order.listener;

import com.parnas.order.dto.event.OrderCreatedEvent;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.model.enumuration.OrderStatus;
import com.parnas.order.service.OrderService;
import com.parnas.order.utils.RabbitMQConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedListener {
    private final OrderService orderService;

    @RabbitListener(queues = RabbitMQConstants.QUEUES.ORDER_CREATED_QUEUE)
    public void handleOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent) {
        log.info("Retrieve order with: id: {}, customerName: {}, totalAmount: {}",
                orderCreatedEvent.orderId(),
                orderCreatedEvent.customerName(),
                orderCreatedEvent.totalAmount());

        orderService.updateOrderStatus(
                orderCreatedEvent.orderId(),
                new OrderUpdateStatusRequest(OrderStatus.PROCESSING)
        );
    }
}
