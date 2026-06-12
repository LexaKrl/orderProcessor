package com.parnas.order.repository;

import com.parnas.order.model.entity.Order;
import org.hibernate.type.descriptor.converter.spi.JpaAttributeConverter;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderRepository extends JpaAttributeConverter<Order, UUID> {

}
