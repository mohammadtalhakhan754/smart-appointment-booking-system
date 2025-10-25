package com.appointment.system.listener;

import com.appointment.system.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class EmailListener {

    /**
     * ✅ FIXED: Added null safety for message processing
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailNotification(Map<String, Object> emailData) {
        try {
            // ✅ CRITICAL: Null safety check
            if (emailData == null || emailData.isEmpty()) {
                log.error("❌ Received null or empty email data");
                return;
            }

            String type = (String) emailData.get("type");

            // ✅ CRITICAL: Null safety for type
            if (type == null) {
                log.error("❌ Email type is null. Message data: {}", emailData);
                log.error("❌ Available keys: {}", emailData.keySet());
                return;
            }

            log.info("🔔 Processing email notification of type: {}", type);

            switch (type) {
                case "APPOINTMENT_CONFIRMATION":
                    sendAppointmentConfirmationEmail(emailData);
                    break;
                case "APPOINTMENT_CANCELLATION":
                    sendAppointmentCancellationEmail(emailData);
                    break;
                case "APPOINTMENT_REMINDER":
                    sendAppointmentReminderEmail(emailData);
                    break;
                default:
                    log.warn("⚠️ Unknown email type: {}", type);
            }

            log.info("✅ Email notification processed successfully");
        } catch (Exception e) {
            log.error("❌ Error processing email notification: {}", e.getMessage(), e);
            // Don't re-throw to prevent infinite retry loop during development
            // throw e; // Uncomment for production
        }
    }

    private void sendAppointmentConfirmationEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String patientName = getStringValue(data, "patientName");
            String doctorName = getStringValue(data, "doctorName");
            String serviceName = getStringValue(data, "serviceName");
            String appointmentDate = getStringValue(data, "appointmentDate");
            String appointmentTime = getStringValue(data, "appointmentTime");
            Long appointmentId = getLongValue(data, "appointmentId");

            log.info("📧 ====================================");
            log.info("📧 SENDING CONFIRMATION EMAIL");
            log.info("📧 ====================================");
            log.info("📧 To: {}", patientEmail);
            log.info("📧 Subject: Appointment Confirmation - ID #{}", appointmentId);
            log.info("📧 ====================================");
            log.info("📧 Dear {},", patientName);
            log.info("📧 ");
            log.info("📧 Your appointment has been successfully confirmed!");
            log.info("📧 ");
            log.info("📧 Appointment Details:");
            log.info("📧 - Doctor: {}", doctorName);
            log.info("📧 - Service: {}", serviceName);
            log.info("📧 - Date: {}", appointmentDate);
            log.info("📧 - Time: {}", appointmentTime);
            log.info("📧 - Appointment ID: #{}", appointmentId);
            log.info("📧 ");
            log.info("📧 Please arrive 10 minutes early.");
            log.info("📧 ");
            log.info("📧 Best regards,");
            log.info("📧 Smart Appointment Booking System");
            log.info("📧 ====================================");
        } catch (Exception e) {
            log.error("❌ Error sending confirmation email: {}", e.getMessage(), e);
        }
    }

    private void sendAppointmentCancellationEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String patientName = getStringValue(data, "patientName");
            Long appointmentId = getLongValue(data, "appointmentId");

            log.info("📧 ====================================");
            log.info("📧 SENDING CANCELLATION EMAIL");
            log.info("📧 ====================================");
            log.info("📧 To: {}", patientEmail);
            log.info("📧 Subject: Appointment Cancelled - ID #{}", appointmentId);
            log.info("📧 ====================================");
            log.info("📧 Dear {},", patientName);
            log.info("📧 ");
            log.info("📧 Your appointment #{} has been cancelled.", appointmentId);
            log.info("📧 ");
            log.info("📧 Best regards,");
            log.info("📧 Smart Appointment Booking System");
            log.info("📧 ====================================");
        } catch (Exception e) {
            log.error("❌ Error sending cancellation email: {}", e.getMessage(), e);
        }
    }

    private void sendAppointmentReminderEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String appointmentDate = getStringValue(data, "appointmentDate");
            String appointmentTime = getStringValue(data, "appointmentTime");
            Long appointmentId = getLongValue(data, "appointmentId");

            log.info("📧 ====================================");
            log.info("📧 SENDING REMINDER EMAIL");
            log.info("📧 ====================================");
            log.info("📧 To: {}", patientEmail);
            log.info("📧 Subject: Appointment Reminder - Tomorrow!");
            log.info("📧 ====================================");
            log.info("📧 Reminder: You have an appointment tomorrow!");
            log.info("📧 - Date: {}", appointmentDate);
            log.info("📧 - Time: {}", appointmentTime);
            log.info("📧 - ID: #{}", appointmentId);
            log.info("📧 ====================================");
        } catch (Exception e) {
            log.error("❌ Error sending reminder email: {}", e.getMessage(), e);
        }
    }

    // ✅ Helper methods for safe data extraction
    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : "N/A";
    }

    private Long getLongValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return 0L;
    }
}
