package com.example.Expense.Tracking.System.Service;


import com.example.Expense.Tracking.System.Entity.*;
import com.example.Expense.Tracking.System.Enum.*;
import com.example.Expense.Tracking.System.Repository.AlertRepository;
import com.example.Expense.Tracking.System.Repository.InventoryItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AlertService {
    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private EmailService emailService;

    public List<Alert> getActiveAlertsByFranchise(Franchise franchise) {
        return alertRepository.findByFranchiseAndResolved(franchise, false);
    }

    public List<Alert> getAllActiveAlerts() {
        return alertRepository.findByResolved(false);
    }

    public Alert createAlert(AlertType type, AlertSeverity severity, String message, String description,
                             InventoryItem inventoryItem, Franchise franchise, User createdBy) {
        Alert alert = new Alert(type, severity, message, description, inventoryItem, franchise, createdBy);
        Alert savedAlert = alertRepository.save(alert);

        // Send email notification for critical and warning alerts
        if (severity == AlertSeverity.CRITICAL || severity == AlertSeverity.WARNING) {
            sendAlertEmail(savedAlert);
        }

        return savedAlert;
    }

    public Alert getAlertById(Long id) {
       Alert alert = alertRepository.findById(id).orElse(null);
        return alert;
    }

    public void resolveLowStockAlertsForItem(InventoryItem item) {
        List<Alert> lowStockAlerts = alertRepository.findByFranchiseAndResolved(item.getFranchise(), false).stream()
                .filter(alert -> (alert.getType() == AlertType.LOW_STOCK || alert.getType() == AlertType.LOW_STOCK_CRITICAL) &&
                        alert.getInventoryItem() != null &&
                        alert.getInventoryItem().getId().equals(item.getId()))
                .collect(Collectors.toList());

        User systemUser = new User("System", "system@moboo.com", "system", UserRole.ADMIN);
        for (Alert alert : lowStockAlerts) {
            resolveAlert(alert.getId(), systemUser);
        }
    }

    public void resolveAlert(Long alertId, User resolvedBy) {
        Alert alert = alertRepository.findById(alertId).orElse(null);
        if (alert != null) {
            alert.setResolved(true);
            alert.setResolvedAt(LocalDateTime.now());
            alert.setResolvedBy(resolvedBy);
            alertRepository.save(alert);
        }
    }

    private void sendAlertEmail(Alert alert) {
        if (alert.getFranchise() != null) {
            emailService.sendExpiryNotification(
                    alert.getFranchise().getEmail(),
                    alert.getFranchise().getName(),
                    alert.getMessage(),
                    alert.getDescription()
            );
        }
    }

    @Transactional
    public void updateAlert(Alert alert) {
        alertRepository.save(alert);
    }

    public void createAlertAndNotify(AlertType type, AlertSeverity severity, String message, String description,
                                     InventoryItem inventoryItem, Franchise franchise, User createdBy) {
        Alert alert = new Alert(type, severity, message, description, inventoryItem, franchise, createdBy);
        Alert savedAlert = alertRepository.save(alert);

        // Send notification for critical and warning alerts
        if (severity == AlertSeverity.CRITICAL || severity == AlertSeverity.WARNING) {
            emailService.sendAlertEmail(savedAlert);
        }
    }

    // Method to get alerts with notification status
    public List<Alert> getAlertsWithNotificationStatus(Franchise franchise, boolean includeResolved) {
        if (franchise != null) {
            return includeResolved ?
                    alertRepository.findByFranchise(franchise) :
                    alertRepository.findByFranchiseAndResolved(franchise, false);
        }
        return includeResolved ?
                alertRepository.findAll() :
                alertRepository.findByResolved(false);
    }

    public boolean alertExistsForItem(InventoryItem item, AlertType alertType) {
        List<Alert> existingAlerts = alertRepository.findByFranchiseAndResolved(item.getFranchise(), false);
        return existingAlerts.stream()
                .anyMatch(alert -> alert.getType() == alertType &&
                        alert.getInventoryItem() != null &&
                        alert.getInventoryItem().getId().equals(item.getId()));
    }

    // Scheduled task to check for inventory issues and create alerts
    @Scheduled(fixedRate = 3600000) // Run every hour
    public void checkInventoryAlerts() {
        List<Franchise> franchises = franchiseService.getAllFranchises();
        User systemUser = new User("System", "system@moboo.com", "system", UserRole.ADMIN);

        for (Franchise franchise : franchises) {
            List<InventoryItem> items = inventoryItemRepository.findByFranchise(franchise);

            for (InventoryItem item : items) {
                LocalDate today = LocalDate.now();

                // Check for expired items
                if (item.getExpiryDate().isBefore(today)) {
                    createAlertIfNotExists(AlertType.EXPIRED_ITEM, AlertSeverity.CRITICAL,
                            "Expired Item: " + item.getName(),
                            "Item expired on " + item.getExpiryDate(),
                            item, franchise, systemUser);
                }
                // Check for expiring soon items
                else if (item.getExpiryDate().isBefore(today.plusDays(7))) {
                    createAlertIfNotExists(AlertType.EXPIRING_SOON, AlertSeverity.WARNING,
                            "Expiring Soon: " + item.getName(),
                            "Item expires on " + item.getExpiryDate(),
                            item, franchise, systemUser);
                }
                // Check for low stock
                else if (item.getCount() < 10) {
                    AlertSeverity severity = item.getCount() == 0 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
                    AlertType type = item.getCount() == 0 ? AlertType.ZERO_STOCK : AlertType.LOW_STOCK;

                    createAlertIfNotExists(type, severity,
                            (item.getCount() == 0 ? "Zero Stock: " : "Low Stock: ") + item.getName(),
                            "Current count: " + item.getCount(),
                            item, franchise, systemUser);
                }
            }
        }
    }

    private void createAlertIfNotExists(AlertType type, AlertSeverity severity, String message,
                                        String description, InventoryItem item, Franchise franchise, User user) {
        // Check if similar unresolved alert already exists
        List<Alert> existingAlerts = alertRepository.findByFranchiseAndResolved(franchise, false);
        boolean alertExists = existingAlerts.stream()
                .anyMatch(alert -> alert.getType() == type &&
                        alert.getInventoryItem() != null &&
                        alert.getInventoryItem().getId().equals(item.getId()));

        if (!alertExists) {
            createAlert(type, severity, message, description, item, franchise, user);
        }
    }

    public long getActiveAlertsCount() {
        return alertRepository.countByResolved(false);
    }

    public long getActiveAlertsCountByFranchise(Franchise franchise) {
        return alertRepository.countByFranchiseAndResolved(franchise, false);
    }

    public long getCriticalAlertsCount() {
        return alertRepository.countBySeverityAndResolved(AlertSeverity.CRITICAL, false);
    }

    public long getWarningAlertsCount() {
        return alertRepository.countBySeverityAndResolved(AlertSeverity.WARNING, false);
    }
}