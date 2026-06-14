package com.parnas.order.config.props;

import com.parnas.order.model.enumuration.OrderStatus;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "application")
public class ApplicationProperties {
    private int defaultOrderItemsPageNumber = 0;
    private int defaultOrderItemsPageSize = 10;
    private String defaultOrderSortValue = "customerName";
    private OrderStatus defaultOrderStatus = OrderStatus.CREATED;
}
