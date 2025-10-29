package com.appointment.system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.liquibase.enabled=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class SmartAppointmentBookingSystemApplicationTests {

    @MockBean
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private RedisTemplate<String, Object> redisTemplate;

    @Test
    void contextLoads() {
        // Context loads successfully
    }
}
