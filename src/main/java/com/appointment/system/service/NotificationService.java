package com.appointment.system.service;

import com.appointment.system.config.RabbitMQConfig;
import com.appointment.system.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final RabbitTemplate rabbitTemplate;

    @Async
    public void scheduleReminder(Appointment appointment) {
        try {
            // Calculate reminder time (24 hours before appointment)
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    appointment.getAppointmentDate(),
                    appointment.getStartTime()
            );

            LocalDateTime reminderTime = appointmentDateTime.minus(24, ChronoUnit.HOURS);

            Map<String, Object> reminderData = new HashMap<>();
            reminderData.put("appointmentId", appointment.getId());
            reminderData.put("patientName", appointment.getPatient().getUser().getFirstName());
            reminderData.put("patientEmail", appointment.getPatient().getUser().getEmail());
            reminderData.put("doctorName", "Dr. " + appointment.getDoctor().getUser().getFirstName() + " " +
                    appointment.getDoctor().getUser().getLastName());
            reminderData.put("appointmentDate", appointment.getAppointmentDate().toString());
            reminderData.put("appointmentTime", appointment.getStartTime().toString());
            reminderData.put("serviceName", appointment.getService().getServiceName());
            reminderData.put("reminderTime", reminderTime.toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    reminderData
            );

            log.info("Reminder scheduled for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error scheduling reminder: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendStatusUpdateNotification(Appointment appointment, String oldStatus, String newStatus) {
        try {
            Map<String, Object> notificationData = new HashMap<>();
            notificationData.put("type", "STATUS_UPDATE");
            notificationData.put("appointmentId", appointment.getId());
            notificationData.put("patientEmail", appointment.getPatient().getUser().getEmail());
            notificationData.put("oldStatus", oldStatus);
            notificationData.put("newStatus", newStatus);
            notificationData.put("appointmentDate", appointment.getAppointmentDate().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.NOTIFICATION_EXCHANGE,
                    RabbitMQConfig.NOTIFICATION_ROUTING_KEY,
                    notificationData
            );

            log.info("Status update notification sent for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("Error sending status update notification: {}", e.getMessage(), e);
        }
    }
}
