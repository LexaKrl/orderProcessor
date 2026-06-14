package com.parnas.order.model.entity;

import com.parnas.order.model.enumuration.OrderStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "orders", indexes = {
        @Index(name = "idx_order_customer_name", columnList = "customer_name"),
        @Index(name = "idx_order_status", columnList = "status")
})
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id")
    private UUID id;

    @Column(name = "customer_name",
            nullable = false)
    private String customerName;

    @CreationTimestamp
    @Column(name = "order_date",
            nullable = false)
    private LocalDateTime orderDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status",
            nullable = false)
    private OrderStatus status = OrderStatus.CREATED;
}
