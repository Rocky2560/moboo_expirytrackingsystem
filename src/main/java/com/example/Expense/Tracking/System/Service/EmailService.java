package com.example.Expense.Tracking.System.Service;

import com.example.Expense.Tracking.System.Entity.Alert;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AlertService alertService;

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Async
    @Transactional
    public void sendAlertEmail(Alert alert) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(alert.getFranchise().getEmail());
            helper.setSubject("Moboo Inventory - " + getAlertSubject(alert));
            helper.setText(createEmailBody(alert), true);
            helper.setFrom("noreply@moboo.com");

            mailSender.send(message);

            // Update alert as successfully sent
            alert.setNotificationSent(true);
            alert.setNotificationSentAt(java.time.LocalDateTime.now());
            alert.setNotificationFailed(false);
            alert.setNotificationErrorMessage(null);
            alertService.updateAlert(alert);

            logger.info("Alert email sent successfully for alert ID: {}", alert.getId());

        } catch (MessagingException e) {
            logger.error("Failed to create email message for alert ID: {}", alert.getId(), e);
            handleEmailFailure(alert, "Email creation failed: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Failed to send email for alert ID: {}", alert.getId(), e);
            handleEmailFailure(alert, "Email sending failed: " + e.getMessage());
        }
    }

    private String getAlertSubject(Alert alert) {
        switch (alert.getType()) {
            case EXPIRED_ITEM:
                return "URGENT: Expired Item Detected";
            case LOW_STOCK:
            case LOW_STOCK_CRITICAL:
                return "WARNING: Low Stock Alert";
            case EXPIRING_SOON:
                return "NOTICE: Item Expiring Soon";
            default:
                return "Inventory Alert";
        }
    }

    private String createEmailBody(Alert alert) {
        return String.format(
                "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px;'>" +
                        "<div style='background: linear-gradient(135deg, #667eea 0%, #764ba2 100%%); color: white; padding: 20px; text-align: center; border-radius: 10px;'>" +
                        "<h2>Moboo Inventory Alert</h2>" +
                        "</div>" +
                        "<div style='background: white; padding: 30px; margin-top: 20px; border-radius: 10px; box-shadow: 0 2px 10px rgba(0,0,0,0.1);'>" +
                        "<h3 style='color: #333;'>%s</h3>" +
                        "<p style='color: #666; line-height: 1.6;'>%s</p>" +
                        "<div style='background: #f8f9fa; padding: 15px; border-left: 4px solid #007bff; margin: 20px 0;'>" +
                        "<strong>Franchise:</strong> %s<br>" +
                        "<strong>Item:</strong> %s<br>" +
                        "<strong>Current Stock:</strong> %s units<br>" +
                        "<strong>Expiry Date:</strong> %s" +
                        "</div>" +
                        "<p style='color: #666;'>Please take immediate action to resolve this issue.</p>" +
                        "<div style='text-align: center; margin-top: 30px; padding-top: 20px; border-top: 1px solid #eee;'>" +
                        "<small style='color: #999;'>This is an automated message from Moboo Inventory System</small>" +
                        "</div>" +
                        "</div>" +
                        "</div>",
                alert.getMessage(),
                alert.getDescription(),
                alert.getFranchise().getName(),
                alert.getInventoryItem().getName(),
                alert.getInventoryItem().getCount(),
                alert.getInventoryItem().getExpiryDate()
        );
    }

    private void handleEmailFailure(Alert alert, String errorMessage) {
        alert.setNotificationFailed(true);
        alert.setNotificationErrorMessage(errorMessage);
        alert.incrementRetryCount();
        alertService.updateAlert(alert);

        logger.warn("Alert email failed for alert ID: {}. Error: {}", alert.getId(), errorMessage);

        // Retry logic (max 3 attempts)
        if (alert.getNotificationRetryCount() < 3) {
            // Schedule retry after 5 minutes
            // This would require additional scheduling logic
        }
    }
    @Async
    public void sendExpiryNotification(String toEmail, String franchiseName, String itemName, String expiryDate) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject("Moboo Inventory - Item Expiry Alert");
        message.setText(String.format(
                "Dear %s,\n\n" +
                        "This is to notify you that the following item has expired or is about to expire:\n\n" +
                        "Item: %s\n" +
                        "Expiry Date: %s\n\n" +
                        "Please take necessary action to remove or replace this item.\n\n" +
                        "Best regards,\n" +
                        "Moboo Inventory System",
                franchiseName, itemName, expiryDate
        ));

        try {
            mailSender.send(message);
        } catch (Exception e) {
            // Log the error but don't throw exception to avoid breaking the main flow
            System.err.println("Failed to send email: " + e.getMessage());
        }
    }
}
