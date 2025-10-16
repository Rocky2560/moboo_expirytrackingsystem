package com.example.Expense.Tracking.System.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

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
