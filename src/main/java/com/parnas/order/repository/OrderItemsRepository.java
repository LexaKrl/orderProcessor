package com.parnas.order.repository;

import com.parnas.order.model.entity.Order;
import com.parnas.order.model.entity.OrderItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemsRepository extends JpaRepository<OrderItem, Long> {

    Page<OrderItem> findByOrder(Order order, Pageable pageable);
}
