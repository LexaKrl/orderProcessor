package com.parnas.order.utils;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public final class SortValidationConstants {
    public static final class Order {
        public static final List<String> LIST_AVAILABLE_SORTS =
                List.of("customerName", "orderDate", "status");
    }
}
