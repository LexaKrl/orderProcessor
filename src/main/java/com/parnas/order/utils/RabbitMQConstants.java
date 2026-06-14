package com.parnas.order.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class RabbitMQConstants {
    public static final class QUEUES {
        public static final String ORDER_CREATED_QUEUE = "order.created";
    }

    public static final class EXCHANGES {
        public static final String ORDER_EXCHANGE = "order.exchange";
    }

    public static final class ROUTINGS {
        public static final String ORDER_CREATED_ROUTING = "order.created";
    }
}
