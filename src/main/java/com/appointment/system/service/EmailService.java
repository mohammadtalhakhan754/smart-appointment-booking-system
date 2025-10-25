package com.appointment.system.service;

import com.appointment.system.config.RabbitMQConfig;
import com.appointment.system.entity.Appointment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final RabbitTemplate rabbitTemplate;

    /**
     * ✅ FIXED: Sends correct message structure to RabbitMQ
     */
    @Async
    public void sendAppointmentConfirmation(Appointment appointment) {
        try {
            Map<String, Object> emailData = new HashMap<>();

            // ✅ CRITICAL: Add the "type" field
            emailData.put("type", "APPOINTMENT_CONFIRMATION");
            emailData.put("appointmentId", appointment.getId());
            emailData.put("patientName", appointment.getPatient().getUser().getFirstName() + " " +
                    appointment.getPatient().getUser().getLastName());
            emailData.put("patientEmail", appointment.getPatient().getUser().getEmail());
            emailData.put("doctorName", "Dr. " + appointment.getDoctor().getUser().getFirstName() +
                    " " + appointment.getDoctor().getUser().getLastName());
            emailData.put("serviceName", appointment.getService().getServiceName());
            emailData.put("appointmentDate", appointment.getAppointmentDate()
                    .format(DateTimeFormatter.ofPattern("dd MMM yyyy")));
            emailData.put("appointmentTime", appointment.getStartTime()
                    .format(DateTimeFormatter.ofPattern("hh:mm a")));

            // ✅ Send to correct exchange and routing key
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailData
            );

            log.info("✅ Email notification queued for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue email notification: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendAppointmentCancellation(Appointment appointment) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("type", "APPOINTMENT_CANCELLATION"); // ✅ CRITICAL: Add type
            emailData.put("appointmentId", appointment.getId());
            emailData.put("patientEmail", appointment.getPatient().getUser().getEmail());
            emailData.put("patientName", appointment.getPatient().getUser().getFirstName() + " " +
                    appointment.getPatient().getUser().getLastName());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailData
            );

            log.info("✅ Cancellation email queued for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue cancellation email: {}", e.getMessage(), e);
        }
    }

    @Async
    public void sendAppointmentReminder(Appointment appointment) {
        try {
            Map<String, Object> emailData = new HashMap<>();
            emailData.put("type", "APPOINTMENT_REMINDER"); // ✅ CRITICAL: Add type
            emailData.put("appointmentId", appointment.getId());
            emailData.put("patientEmail", appointment.getPatient().getUser().getEmail());
            emailData.put("appointmentDate", appointment.getAppointmentDate().toString());
            emailData.put("appointmentTime", appointment.getStartTime().toString());

            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.EMAIL_EXCHANGE,
                    RabbitMQConfig.EMAIL_ROUTING_KEY,
                    emailData
            );

            log.info("✅ Reminder email queued for appointment ID: {}", appointment.getId());
        } catch (Exception e) {
            log.error("❌ Failed to queue reminder email: {}", e.getMessage(), e);
        }
    }
}
