package com.appointment.system.listener;

import com.appointment.system.config.RabbitMQConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class EmailListener {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${app.email.from:noreply@appointmentbooking.com}")
    private String fromEmail;

    @Value("${app.email.from-name:Smart Appointment System}")
    private String fromName;

    @Value("${app.email.enabled:true}")
    private boolean emailEnabled;

    /**
     * ✅ Listens to email queue and sends REAL emails
     */
    @RabbitListener(queues = RabbitMQConfig.EMAIL_QUEUE)
    public void handleEmailNotification(Map<String, Object> emailData) {
        try {
            // Null safety checks
            if (emailData == null || emailData.isEmpty()) {
                log.error("❌ Received null or empty email data");
                return;
            }

            String type = (String) emailData.get("type");

            if (type == null) {
                log.error("❌ Email type is null. Message data: {}", emailData);
                return;
            }

            log.info("🔔 Processing email notification of type: {}", type);

            // Check if emails are enabled
            if (!emailEnabled) {
                log.warn("⚠️ Email sending is DISABLED in configuration. Skipping email.");
                logEmailDetails(type, emailData);
                return;
            }

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
            // Don't re-throw in development to prevent infinite retry
            // throw e; // Uncomment for production
        }
    }

    /**
     * ✅ Send REAL appointment confirmation email
     */
    private void sendAppointmentConfirmationEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String patientName = getStringValue(data, "patientName");
            String doctorName = getStringValue(data, "doctorName");
            String serviceName = getStringValue(data, "serviceName");
            String appointmentDate = getStringValue(data, "appointmentDate");
            String appointmentTime = getStringValue(data, "appointmentTime");
            Long appointmentId = getLongValue(data, "appointmentId");

            // Create email message
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(patientEmail);
            message.setSubject("✅ Appointment Confirmation - ID #" + appointmentId);

            // Email body
            String emailBody = buildConfirmationEmailBody(
                    patientName, doctorName, serviceName,
                    appointmentDate, appointmentTime, appointmentId
            );
            message.setText(emailBody);

            // Send email
            mailSender.send(message);

            log.info("📧 ✅ REAL EMAIL SENT successfully to: {}", patientEmail);
            log.info("📧 Subject: {}", message.getSubject());

        } catch (MailException e) {
            log.error("❌ Failed to send confirmation email: {}", e.getMessage(), e);
            throw e; // Re-throw to trigger retry
        } catch (Exception e) {
            log.error("❌ Unexpected error sending confirmation email: {}", e.getMessage(), e);
        }
    }

    /**
     * ✅ Send REAL appointment cancellation email
     */
    private void sendAppointmentCancellationEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String patientName = getStringValue(data, "patientName");
            Long appointmentId = getLongValue(data, "appointmentId");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(patientEmail);
            message.setSubject("❌ Appointment Cancelled - ID #" + appointmentId);

            String emailBody = buildCancellationEmailBody(patientName, appointmentId);
            message.setText(emailBody);

            mailSender.send(message);

            log.info("📧 ✅ Cancellation email sent to: {}", patientEmail);

        } catch (MailException e) {
            log.error("❌ Failed to send cancellation email: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * ✅ Send REAL appointment reminder email
     */
    private void sendAppointmentReminderEmail(Map<String, Object> data) {
        try {
            String patientEmail = getStringValue(data, "patientEmail");
            String appointmentDate = getStringValue(data, "appointmentDate");
            String appointmentTime = getStringValue(data, "appointmentTime");
            Long appointmentId = getLongValue(data, "appointmentId");

            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromName + " <" + fromEmail + ">");
            message.setTo(patientEmail);
            message.setSubject("⏰ Appointment Reminder - Tomorrow!");

            String emailBody = buildReminderEmailBody(appointmentDate, appointmentTime, appointmentId);
            message.setText(emailBody);

            mailSender.send(message);

            log.info("📧 ✅ Reminder email sent to: {}", patientEmail);

        } catch (MailException e) {
            log.error("❌ Failed to send reminder email: {}", e.getMessage(), e);
            throw e;
        }
    }

    /**
     * ✅ Build professional confirmation email body
     */
    private String buildConfirmationEmailBody(String patientName, String doctorName,
                                              String serviceName, String appointmentDate,
                                              String appointmentTime, Long appointmentId) {
        return String.format("""
            Dear %s,
            
            Your appointment has been successfully confirmed!
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            APPOINTMENT DETAILS
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Appointment ID:  #%d
            Doctor:          %s
            Service:         %s
            Date:            %s
            Time:            %s
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            IMPORTANT INFORMATION:
            • Please arrive 10 minutes before your scheduled time
            • Bring your ID and insurance card (if applicable)
            • If you need to cancel or reschedule, please do so at least 24 hours in advance
            
            If you have any questions, please contact us.
            
            Best regards,
            Smart Appointment Booking System
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply to this email.
            """,
                patientName, appointmentId, doctorName, serviceName,
                appointmentDate, appointmentTime
        );
    }

    /**
     * ✅ Build cancellation email body
     */
    private String buildCancellationEmailBody(String patientName, Long appointmentId) {
        return String.format("""
            Dear %s,
            
            Your appointment #%d has been cancelled.
            
            If you did not request this cancellation or if you believe this is an error,
            please contact us immediately.
            
            To book a new appointment, please visit our website or call us.
            
            Best regards,
            Smart Appointment Booking System
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            This is an automated message. Please do not reply to this email.
            """,
                patientName, appointmentId
        );
    }

    /**
     * ✅ Build reminder email body
     */
    private String buildReminderEmailBody(String appointmentDate, String appointmentTime, Long appointmentId) {
        return String.format("""
            REMINDER: You have an appointment tomorrow!
            
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            Appointment ID:  #%d
            Date:            %s
            Time:            %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            
            Please arrive 10 minutes early.
            
            See you soon!
            
            Best regards,
            Smart Appointment Booking System
            """,
                appointmentId, appointmentDate, appointmentTime
        );
    }

    /**
     * Log email details (for when emails are disabled)
     */
    private void logEmailDetails(String type, Map<String, Object> data) {
        log.info("📧 ====================================");
        log.info("📧 EMAIL TYPE: {}", type);
        log.info("📧 EMAIL DATA: {}", data);
        log.info("📧 ====================================");
    }

    // Helper methods for safe data extraction
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
