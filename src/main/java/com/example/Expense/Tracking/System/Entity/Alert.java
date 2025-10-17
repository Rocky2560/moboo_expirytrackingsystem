package com.example.Expense.Tracking.System.Entity;

import com.example.Expense.Tracking.System.Enum.AlertType;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.example.Expense.Tracking.System.Enum.*;

@Entity
@Table(name = "alerts")
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlertType type;

    @Enumerated(EnumType.STRING)
    private AlertSeverity severity;

    private String message;
    private String description;
    private boolean resolved;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @ManyToOne
    @JoinColumn(name = "franchise_id")
    private Franchise franchise;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne
    @JoinColumn(name = "resolved_by_user_id")
    private User resolvedBy;


    // Notification tracking fields
    private boolean notificationSent = false;
    private boolean notificationFailed = false;
    private String notificationErrorMessage;
    private LocalDateTime notificationSentAt;
    private int notificationRetryCount = 0;

    public Alert() {
    }

    public Alert(AlertType type, AlertSeverity severity, String message, String description,
                 InventoryItem inventoryItem, Franchise franchise, User createdBy) {
        this.type = type;
        this.severity = severity;
        this.message = message;
        this.description = description;
        this.inventoryItem = inventoryItem;
        this.franchise = franchise;
        this.createdBy = createdBy;
        this.resolved = false;
        this.createdAt = LocalDateTime.now();
    }

    // Getters and Setters (including new notification fields)
    public boolean isNotificationSent() { return notificationSent; }
    public void setNotificationSent(boolean notificationSent) { this.notificationSent = notificationSent; }

    public boolean isNotificationFailed() { return notificationFailed; }
    public void setNotificationFailed(boolean notificationFailed) { this.notificationFailed = notificationFailed; }

    public String getNotificationErrorMessage() { return notificationErrorMessage; }
    public void setNotificationErrorMessage(String notificationErrorMessage) { this.notificationErrorMessage = notificationErrorMessage; }

    public LocalDateTime getNotificationSentAt() { return notificationSentAt; }
    public void setNotificationSentAt(LocalDateTime notificationSentAt) { this.notificationSentAt = notificationSentAt; }

    public int getNotificationRetryCount() { return notificationRetryCount; }
    public void setNotificationRetryCount(int notificationRetryCount) { this.notificationRetryCount = notificationRetryCount; }

    public void incrementRetryCount() { this.notificationRetryCount++; }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public AlertType getType() { return type; }
    public void setType(AlertType type) { this.type = type; }

    public AlertSeverity getSeverity() { return severity; }
    public void setSeverity(AlertSeverity severity) { this.severity = severity; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public boolean isResolved() { return resolved; }
    public void setResolved(boolean resolved) { this.resolved = resolved; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getResolvedAt() { return resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }

    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }

    public Franchise getFranchise() { return franchise; }
    public void setFranchise(Franchise franchise) { this.franchise = franchise; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public User getResolvedBy() { return resolvedBy; }
    public void setResolvedBy(User resolvedBy) { this.resolvedBy = resolvedBy; }
}