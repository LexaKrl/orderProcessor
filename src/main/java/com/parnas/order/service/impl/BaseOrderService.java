package com.parnas.order.service.impl;

import com.parnas.order.config.props.ApplicationProperties;
import com.parnas.order.dto.request.OrderRequest;
import com.parnas.order.dto.response.OrderResponse;
import com.parnas.order.exception.order.OrderBadRequestException;
import com.parnas.order.exception.order.OrderNotFoundException;
import com.parnas.order.mapstruct.OrderMapper;
import com.parnas.order.model.entity.Order;
import com.parnas.order.model.enumuration.OrderStatus;
import com.parnas.order.repository.OrderItemsRepository;
import com.parnas.order.repository.OrderRepository;
import com.parnas.order.service.OrderService;
import com.parnas.order.utils.SortValidationConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class BaseOrderService implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemsRepository orderItemsRepository;
    private final OrderMapper orderMapper;
    private final ApplicationProperties applicationProperties;

    @Override
    @Transactional
    public void saveOrder(OrderRequest orderRequest) {
        Order retrivedOrder = orderRepository.save(orderMapper.toEntity(orderRequest));
        orderItemsRepository.saveAll(orderMapper.toOrderItemList(orderRequest.items(), retrivedOrder));
        log.info("Saved order with id {}", retrivedOrder.getId());
    }

     /**  Retrieves all orders by OrderStatus and sort with pagination.
    *   Returns fixed amount of OrderItems for each Order.
    *   To get more detailed response from order:
    *   @see #getOrderByIdPageable(UUID id, int orderItemsPage, int orderItemsSize)
    * */
    @Override
    public Page<OrderResponse> getOrdersByStatus(
            OrderStatus status, String sort,
            int page, int size) {
        String actualSort = (sort == null)
                ? applicationProperties.getDefaultOrderSortValue()
                : sort;

        boolean sortSupported = false;
        for (String sortCandidate : SortValidationConstants.Order.LIST_AVAILABLE_SORTS) {
            if (sortCandidate.equals(actualSort)) {
                sortSupported = true;
                break;
            }
        }

        if (!sortSupported) {throw new OrderBadRequestException(actualSort);}

        OrderStatus actualStatus = (status == null)
                ? applicationProperties.getDefaultOrderStatus()
                : status;

        return orderRepository.findAllByStatus(actualStatus, PageRequest.of(page, size, Sort.by(actualSort))) /* find Page<Order> */
                .map(order -> orderMapper /* map from Order to OrderResponse */
                        .toResponse(order, orderItemsRepository /* find OrderItems according each Order with defaults */
                                .findByOrder(order, PageRequest.of(
                                        applicationProperties.getDefaultOrderItemsPageNumber(),
                                        applicationProperties.getDefaultOrderItemsPageSize()))));

    }

    @Override
    public OrderResponse getOrderByIdPageable(UUID id, int orderItemsPage, int orderItemsSize) {
        Order foundOrder = orderRepository
                .findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        return orderMapper.toResponse(
                foundOrder,
                orderItemsRepository
                        .findByOrder(foundOrder, PageRequest.of(orderItemsPage, orderItemsSize))
        );
    }
}
