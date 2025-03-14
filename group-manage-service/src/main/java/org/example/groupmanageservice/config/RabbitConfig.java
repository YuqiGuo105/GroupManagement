package org.example.groupmanageservice.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // Exchange and routing key definitions
    public static final String ROOM_EXCHANGE = "roomExchange";
    public static final String ROUTING_KEY = "room.events";
    public static final String ROOM_QUEUE = "roomQueue";

    @Bean
    public TopicExchange roomExchange() {
        return new TopicExchange(ROOM_EXCHANGE, true, false);
    }

    @Bean
    public Queue roomQueue() {
        return QueueBuilder.durable(ROOM_QUEUE).build();
    }

    @Bean
    public Binding roomBinding(Queue roomQueue, TopicExchange roomExchange) {
        return BindingBuilder.bind(roomQueue).to(roomExchange).with(ROUTING_KEY);
    }
}
