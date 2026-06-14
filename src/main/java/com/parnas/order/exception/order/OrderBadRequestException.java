package com.parnas.order.exception.order;

import com.parnas.order.exception.base.BadRequestException;
import com.parnas.order.utils.SortValidationConstants;

public class OrderBadRequestException extends BadRequestException {
    public OrderBadRequestException(String badSortParameter) {
        super("Sort is not supported for value: %s \nSupported sort values: %s".formatted(
                badSortParameter,
                SortValidationConstants.Order.LIST_AVAILABLE_SORTS.toString())
        );
    }
}
