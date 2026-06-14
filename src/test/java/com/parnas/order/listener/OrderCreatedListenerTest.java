package com.parnas.order.listener;

import com.parnas.order.dto.event.OrderCreatedEvent;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.exception.order.OrderNotFoundException;
import com.parnas.order.model.enumuration.OrderStatus;
import com.parnas.order.service.impl.BaseOrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class OrderCreatedListenerTest {

    @Mock
    private BaseOrderService orderService;

    @InjectMocks
    private OrderCreatedListener orderCreatedListener;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final String CUSTOMER_NAME = "LexaRt";
    private static final BigDecimal TOTAL_AMOUNT = BigDecimal.valueOf(250.0);

    @Test
    void handleOrderCreatedEvent_ShouldCallUpdateOrderStatusWithProcessingStatus() {
        OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, CUSTOMER_NAME, TOTAL_AMOUNT);

        orderCreatedListener.handleOrderCreatedEvent(event);

        verify(orderService).updateOrderStatus(
                eq(ORDER_ID),
                argThat((OrderUpdateStatusRequest req) ->
                        req.status() == OrderStatus.PROCESSING)
        );
    }

    @Test
    void handleOrderCreatedEvent_WhenUpdateFails_ShouldPropagateException() {
        OrderCreatedEvent event = new OrderCreatedEvent(ORDER_ID, CUSTOMER_NAME, TOTAL_AMOUNT);
        OrderNotFoundException exception = new OrderNotFoundException(ORDER_ID);
        doThrow(exception).when(orderService).updateOrderStatus(eq(ORDER_ID), any());

        assertThatThrownBy(() -> orderCreatedListener.handleOrderCreatedEvent(event))
                .isInstanceOf(OrderNotFoundException.class);

        verify(orderService).updateOrderStatus(eq(ORDER_ID), any());
    }

}
