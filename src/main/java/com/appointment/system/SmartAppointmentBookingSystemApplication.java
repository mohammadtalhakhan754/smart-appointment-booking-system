package com.appointment.system;

import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@ComponentScan(basePackages = {
        "com.appointment.system",   // base package for your code
        "com.appointment.system.repository",
        "com.appointment.system.controller",
        "com.appointment.system.config",
        "com.appointment.system.dto",
        "com.appointment.system.entity",
        "com.appointment.system.exception",
        "com.appointment.system.security",
        "com.appointment.system.service"          // add others if needed
})
@EnableCaching
@EnableAsync
@EnableTransactionManagement
@EnableJpaAuditing
@EnableRabbit
public class SmartAppointmentBookingSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmartAppointmentBookingSystemApplication.class, args);
        System.out.println("\n===========================================");
        System.out.println("Smart Appointment Booking System Started!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("===========================================\n");
    }
}
