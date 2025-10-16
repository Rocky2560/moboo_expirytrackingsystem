package com.example.Expense.Tracking.System.Entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_adjustments")
public class InventoryAdjustment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer oldCount;
    private Integer newCount;
    private String reason;
    private LocalDateTime adjustedAt;

    @ManyToOne
    @JoinColumn(name = "inventory_item_id")
    private InventoryItem inventoryItem;

    @ManyToOne
    @JoinColumn(name = "adjusted_by_user_id")
    private User adjustedBy;

    // Constructors
    public InventoryAdjustment() {}

    public InventoryAdjustment(Integer oldCount, Integer newCount, String reason,
                               InventoryItem inventoryItem, User adjustedBy) {
        this.oldCount = oldCount;
        this.newCount = newCount;
        this.reason = reason;
        this.inventoryItem = inventoryItem;
        this.adjustedBy = adjustedBy;
        this.adjustedAt = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Integer getOldCount() { return oldCount; }
    public void setOldCount(Integer oldCount) { this.oldCount = oldCount; }

    public Integer getNewCount() { return newCount; }
    public void setNewCount(Integer newCount) { this.newCount = newCount; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }

    public LocalDateTime getAdjustedAt() { return adjustedAt; }
    public void setAdjustedAt(LocalDateTime adjustedAt) { this.adjustedAt = adjustedAt; }

    public InventoryItem getInventoryItem() { return inventoryItem; }
    public void setInventoryItem(InventoryItem inventoryItem) { this.inventoryItem = inventoryItem; }

    public User getAdjustedBy() { return adjustedBy; }
    public void setAdjustedBy(User adjustedBy) { this.adjustedBy = adjustedBy; }
}
