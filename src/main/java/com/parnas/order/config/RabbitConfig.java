package com.parnas.order.config;

import com.parnas.order.utils.RabbitMQConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    @Bean
    public Queue orderCreatedQueue() {
        return new Queue(
                RabbitMQConstants.QUEUES.ORDER_CREATED_QUEUE,
                true
        );
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(
                RabbitMQConstants.EXCHANGES.ORDER_EXCHANGE
        );
    }

    @Bean
    public Binding orderCreatedBinding(
            @Qualifier("orderCreatedQueue") Queue queue,
            @Qualifier("orderExchange") DirectExchange exchange) {
        return BindingBuilder
                .bind(queue)
                .to(exchange)
                .with(RabbitMQConstants.ROUTINGS.ORDER_CREATED_ROUTING);
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory, MessageConverter converter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);
        return factory;
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}