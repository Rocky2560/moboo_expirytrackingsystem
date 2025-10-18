package com.example.Expense.Tracking.System.Service;


import com.example.Expense.Tracking.System.Entity.*;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Enum.AlertType;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Repository.InventoryAdjustmentRepository;
import com.example.Expense.Tracking.System.Repository.InventoryItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class InventoryService {
    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Autowired
    private InventoryAdjustmentRepository inventoryAdjustmentRepository;

    @Autowired
    private AlertService alertService;

    public List<InventoryItem> getAllItems() {
        return inventoryItemRepository.findAll();
    }

    public List<InventoryItem> getItemsByFranchise(Franchise franchise) {
        return inventoryItemRepository.findByFranchise(franchise);
    }

//    public List<InventoryItem> getItemsByFranchise(Franchise franchise) {
//        return inventoryItemRepository.findByFranchise(franchise);
//    }

//    public InventoryItem saveItem(InventoryItem item) {
//        return inventoryItemRepository.save(item);
//    }



    public List<InventoryItem> getExpiredItems(Franchise franchise) {
        return inventoryItemRepository.findExpiredItems(franchise, LocalDate.now());
    }

    public List<InventoryItem> getExpiringSoonItems(Franchise franchise) {
        return inventoryItemRepository.findExpiringSoonItems(franchise,
                LocalDate.now(),
                LocalDate.now().plusDays(7));
    }

    public List<InventoryItem> getLowStockItems(Franchise franchise) {
        return inventoryItemRepository.findLowStockItems(franchise);
    }

    public int getTotalItems(Franchise franchise) {
        return inventoryItemRepository.findByFranchise(franchise).size();
    }

    public int getTotalItems() {
        return (int) inventoryItemRepository.count();
    }

    public List<InventoryItem> getAllItemsWithStatus() {
        return inventoryItemRepository.findAll().stream()
                .peek(item -> {
                    // Status is calculated on-the-fly
                })
                .collect(Collectors.toList());
    }

    @Transactional
    public InventoryItem saveItem(InventoryItem item) {
        return inventoryItemRepository.save(item);
    }

    @Transactional
    public void deleteItem(Long id) {
        // Also delete any associated adjustment records
        InventoryItem item = inventoryItemRepository.findById(id).orElse(null);
        if (item != null) {
            inventoryAdjustmentRepository.deleteByInventoryItem(item);
            inventoryItemRepository.deleteById(id);
        }
    }

    public Optional<InventoryItem> findById(Long id) {
        return inventoryItemRepository.findById(id);
    }

    @Transactional
    public void adjustItemCount(Long itemId, Integer newCount, String reason, User adjustedBy) {
        InventoryItem item = inventoryItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            Integer oldCount = item.getCount();
            item.setCount(newCount);
            inventoryItemRepository.save(item);

            // Create adjustment record
            InventoryAdjustment adjustment = new InventoryAdjustment(oldCount, newCount, reason, item, adjustedBy);
            inventoryAdjustmentRepository.save(adjustment);

            // Check and create alerts for low stock
            checkAndCreateLowStockAlerts(item, adjustedBy);
        }
    }

    // Method to check low stock and create alerts
    private void checkAndCreateLowStockAlerts(InventoryItem item, User triggeredBy) {
        // Critical low stock (below 5 units)
        if (item.getCount() != null && item.getCount() < 5 && item.getCount() >= 0) {
            // Check if alert already exists for this item
            boolean alertExists = alertService.alertExistsForItem(item, AlertType.LOW_STOCK_CRITICAL);

            if (!alertExists) {
                String message = "Critical Low Stock: " + item.getName();
                String description = "Stock level is " + item.getCount() + " units (below threshold of 5). Immediate action required.";

                alertService.createAlertAndNotify(
                        AlertType.LOW_STOCK_CRITICAL,
                        AlertSeverity.CRITICAL,
                        message,
                        description,
                        item,
                        item.getFranchise(),
                        triggeredBy != null ? triggeredBy : getSystemUser()
                );
            }
        }
        // Warning low stock (below 10 units but above 5)
        else if (item.getCount() != null && item.getCount() < 10 && item.getCount() >= 5) {
            boolean alertExists = alertService.alertExistsForItem(item, AlertType.LOW_STOCK);

            if (!alertExists) {
                String message = "Low Stock Warning: " + item.getName();
                String description = "Stock level is " + item.getCount() + " units (below threshold of 10). Consider reordering soon.";

                alertService.createAlertAndNotify(
                        AlertType.LOW_STOCK,
                        AlertSeverity.WARNING,
                        message,
                        description,
                        item,
                        item.getFranchise(),
                        triggeredBy != null ? triggeredBy : getSystemUser()
                );
            }
        }
        // Clear low stock alerts if stock is restored
        else if (item.getCount() != null && item.getCount() >= 10) {
            alertService.resolveLowStockAlertsForItem(item);
        }
    }

    // Add this to your InventoryService or create a separate ScheduledService
    @Scheduled(fixedRate = 300000) // Check every 5 minutes
    public void monitorLowStockItems() {
        List<InventoryItem> allItems = inventoryItemRepository.findAll();
        User systemUser = new User("System", "system@moboo.com", "system", UserRole.ADMIN);

        for (InventoryItem item : allItems) {
            // Only check items that haven't been recently adjusted (to avoid duplicate alerts)
            if (item.getCount() != null) {
                if (item.getCount() < 5) {
                    // Critical low stock
                    boolean alertExists = alertService.alertExistsForItem(item, AlertType.LOW_STOCK_CRITICAL);
                    if (!alertExists) {
                        String message = "Critical Low Stock: " + item.getName();
                        String description = "Stock level is " + item.getCount() + " units (below threshold of 5). Immediate action required.";

                        alertService.createAlertAndNotify(
                                AlertType.LOW_STOCK_CRITICAL,
                                AlertSeverity.CRITICAL,
                                message,
                                description,
                                item,
                                item.getFranchise(),
                                systemUser
                        );
                    }
                } else if (item.getCount() < 10) {
                    // Warning low stock
                    boolean alertExists = alertService.alertExistsForItem(item, AlertType.LOW_STOCK);
                    if (!alertExists) {
                        String message = "Low Stock Warning: " + item.getName();
                        String description = "Stock level is " + item.getCount() + " units (below threshold of 10). Consider reordering soon.";

                        alertService.createAlertAndNotify(
                                AlertType.LOW_STOCK,
                                AlertSeverity.WARNING,
                                message,
                                description,
                                item,
                                item.getFranchise(),
                                systemUser
                        );
                    }
                }
            }
        }
    }

    private User getSystemUser() {
        return new User("System", "system@moboo.com", "system", UserRole.ADMIN);
    }

    public List<InventoryAdjustment> getAdjustmentHistory(Long itemId) {
        InventoryItem item = inventoryItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            return inventoryAdjustmentRepository.findByInventoryItem(item);
        }
        return List.of();
    }

    public List<InventoryItem> searchItems(String searchTerm, String category, Franchise franchise) {
        List<InventoryItem> items = franchise != null ?
                inventoryItemRepository.findByFranchise(franchise) :
                inventoryItemRepository.findAll();

        return items.stream()
                .filter(item -> (searchTerm == null || searchTerm.isEmpty() ||
                        item.getName().toLowerCase().contains(searchTerm.toLowerCase()) ||
                        item.getCategory().toLowerCase().contains(searchTerm.toLowerCase())))
                .filter(item -> (category == null || category.equals("All") ||
                        item.getCategory().equals(category)))
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return inventoryItemRepository.findAll().stream()
                .map(InventoryItem::getCategory)
                .distinct()
                .collect(Collectors.toList());
    }

    public List<InventoryItem> getItemsWithoutFranchise() {
        return inventoryItemRepository.findByFranchiseIsNull();
    }
}