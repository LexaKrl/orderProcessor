package com.parnas.order.service.impl;

import com.parnas.order.config.props.ApplicationProperties;
import com.parnas.order.dto.event.OrderCreatedEvent;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.request.OrderUpdateStatusRequest;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.exception.base.ServiceException;
import com.parnas.order.exception.order.OrderNotFoundException;
import com.parnas.order.mapstruct.OrderMapper;
import com.parnas.order.model.entity.Order;
import com.parnas.order.model.entity.OrderItem;
import com.parnas.order.model.enumuration.OrderStatus;
import com.parnas.order.repository.OrderItemsRepository;
import com.parnas.order.repository.OrderRepository;
import com.parnas.order.utils.RabbitMQConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BaseOrderServiceTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemsRepository orderItemsRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private ApplicationProperties applicationProperties;

    @InjectMocks
    private BaseOrderService orderService;

    private static final UUID ORDER_ID = UUID.randomUUID();
    private static final String CUSTOMER_NAME = "Test Customer";
    private static final OrderStatus STATUS = OrderStatus.CREATED;
    private static final String VALID_SORT = "customerName";
    private static final String INVALID_SORT = "invalidField";

    private OrderRequest orderRequest;
    private Order order;
    private OrderResponse orderResponse;
    private OrderUpdateStatusRequest updateStatusRequest;

    @BeforeEach
    void setUp() {
        order = new Order();
        order.setId(ORDER_ID);
        order.setCustomerName(CUSTOMER_NAME);
        order.setStatus(STATUS);

        orderRequest = mock(OrderRequest.class);
        orderResponse = mock(OrderResponse.class);
        updateStatusRequest = new OrderUpdateStatusRequest(OrderStatus.PROCESSING);
    }

    @Test
    void saveOrder_ShouldSaveOrderAndItems_AndSendEvent() {
        Order savedOrder = order;
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(savedOrder);

        List<OrderItem> items = Collections.emptyList();
        when(orderMapper.toOrderItemList(eq(orderRequest.items()), eq(savedOrder))).thenReturn(items);
        when(orderRepository.calculateTotal(CUSTOMER_NAME)).thenReturn(BigDecimal.valueOf(150.0));

        orderService.saveOrder(orderRequest);

        verify(orderItemsRepository).saveAll(items);
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);

        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConstants.EXCHANGES.ORDER_EXCHANGE),
                eq(RabbitMQConstants.QUEUES.ORDER_CREATED_QUEUE),
                eventCaptor.capture()
        );

        OrderCreatedEvent event = eventCaptor.getValue();
        assertThat(event)
                .extracting(OrderCreatedEvent::orderId, OrderCreatedEvent::customerName, OrderCreatedEvent::totalAmount)
                .containsExactly(ORDER_ID, CUSTOMER_NAME, BigDecimal.valueOf(150.0));
    }

    @Test
    void saveOrder_WhenOrderRepositorySaveFails_ShouldNotSaveItemsOrSendEvent() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenThrow(new RuntimeException("DB error"));

        assertThatThrownBy(() -> orderService.saveOrder(orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("DB error");

        verify(orderItemsRepository, never()).saveAll(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void saveOrder_WhenOrderItemsRepositorySaveAllFails_ShouldNotSendEvent() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        List<OrderItem> items = Collections.emptyList();
        when(orderMapper.toOrderItemList(any(), eq(order))).thenReturn(items);
        when(orderItemsRepository.saveAll(items)).thenThrow(new RuntimeException("Items save error"));

        assertThatThrownBy(() -> orderService.saveOrder(orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Items save error");

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void getOrdersByStatus_WithValidSortAndStatus_ShouldReturnPage() {
        when(applicationProperties.getDefaultOrderItemsPageSize()).thenReturn(10);

        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByStatus(eq(STATUS), any(Pageable.class))).thenReturn(orderPage);
        Page<OrderItem> itemPage = Page.empty();
        when(orderItemsRepository.findByOrder(eq(order), any(Pageable.class))).thenReturn(itemPage);
        when(orderMapper.toResponse(order, itemPage)).thenReturn(orderResponse);

        Page<OrderResponse> result = orderService.getOrdersByStatus(STATUS, VALID_SORT, 0, 10);

        assertThat(result).hasSize(1);
        verify(orderRepository).findAllByStatus(eq(STATUS), argThat(pageable ->
                pageable.getPageNumber() == 0 && pageable.getPageSize() == 10 &&
                        pageable.getSort().getOrderFor(VALID_SORT) != null
        ));
    }

    @Test
    void getOrdersByStatus_WithEmptySort_ShouldThrowOrderBadRequestException() {
        assertThatThrownBy(() -> orderService.getOrdersByStatus(STATUS, "", 0, 10))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Sort is not supported for value:  \nSupported sort values: [customerName, orderDate, status]");
    }

    @Test
    void getOrdersByStatus_WithInvalidSort_ShouldThrowOrderBadRequestException() {
        assertThatThrownBy(() -> orderService.getOrdersByStatus(STATUS, INVALID_SORT, 0, 10))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("Sort is not supported for value: %s \nSupported sort values: [customerName, orderDate, status]".formatted(INVALID_SORT));
    }

    @Test
    void getOrdersByStatus_WithNullSort_ShouldReturnDefaultSort() {
        when(applicationProperties.getDefaultOrderSortValue()).thenReturn("customerName");
        when(applicationProperties.getDefaultOrderItemsPageSize()).thenReturn(10);

        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByStatus(eq(STATUS), any(Pageable.class))).thenReturn(orderPage);

        Page<OrderItem> itemPage = Page.empty();
        when(orderItemsRepository.findByOrder(eq(order), any(Pageable.class))).thenReturn(itemPage);
        when(orderMapper.toResponse(order, itemPage)).thenReturn(orderResponse);

        assertThat(orderService.getOrdersByStatus(STATUS, null, 0, 10)).hasSize(1);

        verify(orderRepository).findAllByStatus(eq(STATUS), argThat(pageable ->
                pageable.getPageNumber() == 0 &&
                        pageable.getPageSize() == 10 &&
                        pageable.getSort().getOrderFor("customerName") != null
        ));
    }

    @Test
    void getOrdersByStatus_WithNullStatus_ShouldReturnDefaultStatus() {
        when(applicationProperties.getDefaultOrderStatus()).thenReturn(STATUS);
        when(applicationProperties.getDefaultOrderItemsPageSize()).thenReturn(10);

        Page<Order> orderPage = new PageImpl<>(List.of(order));
        when(orderRepository.findAllByStatus(eq(STATUS), any(Pageable.class))).thenReturn(orderPage);

        Page<OrderItem> itemPage = Page.empty();
        when(orderItemsRepository.findByOrder(eq(order), any(Pageable.class))).thenReturn(itemPage);
        when(orderMapper.toResponse(order, itemPage)).thenReturn(orderResponse);

        assertThatNoException().isThrownBy(() -> orderService.getOrdersByStatus(null, VALID_SORT, 0, 10));
    }

    @Test
    void getOrderByIdPageable_WhenOrderExists_ShouldReturnResponse() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
        Page<OrderItem> itemPage = Page.empty();
        when(orderItemsRepository.findByOrder(eq(order), any())).thenReturn(itemPage);
        when(orderMapper.toResponse(order, itemPage)).thenReturn(orderResponse);

        assertThat(orderService.getOrderByIdPageable(ORDER_ID, 0, 5)).isEqualTo(orderResponse);
        verify(orderItemsRepository).findByOrder(eq(order), argThat(p ->
                p.getPageNumber() == 0 && p.getPageSize() == 5));
    }

    @Test
    void getOrderByIdPageable_WhenOrderDoesntExists_ShouldThrowOrderNotFoundException() {
        when(orderRepository.findById(ORDER_ID)).thenThrow(new OrderNotFoundException(ORDER_ID));

        assertThatThrownBy(() -> orderService.getOrderByIdPageable(ORDER_ID, 0, 5))
                .isExactlyInstanceOf(OrderNotFoundException.class)
                .hasMessageContainingAll("Order not found with id: %s".formatted(ORDER_ID));
    }

    @Test
    void updateOrderStatus_WhenOrderExists_ShouldUpdateAndSave() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));

        orderService.updateOrderStatus(ORDER_ID, updateStatusRequest);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.PROCESSING);
        verify(orderRepository).save(order);
    }

    @Test
    void updateOrderStatus_WhenOrderNotFound_ShouldThrowException() {
        when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.updateOrderStatus(ORDER_ID, updateStatusRequest))
                .isInstanceOf(OrderNotFoundException.class);
        verify(orderRepository, never()).save(any());
    }

    @Test
    void saveOrder_WhenCalculateTotalFails_ShouldNotSendEvent() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        List<OrderItem> items = Collections.emptyList();
        when(orderMapper.toOrderItemList(any(), eq(order))).thenReturn(items);
        when(orderRepository.calculateTotal(CUSTOMER_NAME)).thenThrow(new RuntimeException("Calculation error"));

        assertThatThrownBy(() -> orderService.saveOrder(orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Calculation error");

        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(Object.class));
    }

    @Test
    void saveOrder_WhenRabbitTemplateThrowsException_ShouldPropagateException() {
        when(orderMapper.toEntity(orderRequest)).thenReturn(order);
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toOrderItemList(any(), eq(order))).thenReturn(Collections.emptyList());
        when(orderRepository.calculateTotal(CUSTOMER_NAME)).thenReturn(BigDecimal.TEN);
        doThrow(new RuntimeException("RabbitMQ error"))
                .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(OrderCreatedEvent.class));

        assertThatThrownBy(() -> orderService.saveOrder(orderRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("RabbitMQ error");
    }
}
