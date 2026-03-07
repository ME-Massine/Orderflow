package com.massine.orderflow.orderservice.messaging.config;

import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.ConditionalRejectingErrorHandler;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitListenerRetryConfig {

    @Value("${orderflow.rabbitmq.dlx-exchange}")
    private String dlxExchange;

    @Value("${orderflow.rabbitmq.order-created.dlq-routing-key}")
    private String dlqRoutingKey;

    @Bean
    public Jackson2JsonMessageConverter jackson2JsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(
            ConnectionFactory connectionFactory,
            Jackson2JsonMessageConverter converter
    ) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(converter);
        return template;
    }

    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            RabbitTemplate rabbitTemplate,
            Jackson2JsonMessageConverter converter
    ) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(converter);

        RepublishMessageRecoverer recoverer = new RepublishMessageRecoverer(
                rabbitTemplate,
                dlxExchange,
                dlqRoutingKey
        ) {
            @Override
            protected java.util.Map<? extends String, ?> additionalHeaders(
                    org.springframework.amqp.core.Message message,
                    Throwable cause
            ) {
                return java.util.Map.of(
                        "x-original-exchange", message.getMessageProperties().getReceivedExchange(),
                        "x-original-routingKey", message.getMessageProperties().getReceivedRoutingKey()
                );
            }
        };

        factory.setAdviceChain(
                RetryInterceptorBuilder.stateless()
                        .maxAttempts(3)
                        .backOffOptions(1000, 2.0, 8000)
                        .recoverer(recoverer)
                        .build()
        );

        factory.setErrorHandler(new ConditionalRejectingErrorHandler());

        return factory;
    }
}