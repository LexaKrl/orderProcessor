package com.parnas.order.repository;

import com.parnas.order.model.entity.Order;
import com.parnas.order.model.enumuration.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Page<Order> findAllByStatus(OrderStatus status, Pageable pageable);

    @Query(value = """
        SELECT COALESCE(SUM(oi.price * oi.quantity), 0)
        FROM order_items oi
        JOIN orders o ON oi.order_id = o.id
        WHERE o.customer_name = :customerName
    """, nativeQuery = true)
    BigDecimal calculateTotal(@Param("customerName") String customerName);
}
