package com.example.Expense.Tracking.System.Service;


import com.example.Expense.Tracking.System.Entity.*;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Enum.AlertType;
import com.example.Expense.Tracking.System.Repository.InventoryAdjustmentRepository;
import com.example.Expense.Tracking.System.Repository.InventoryItemRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
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
    public void adjustItemCount(Long itemId, Integer newCount, String reason, User adjustedBy) {
        InventoryItem item = inventoryItemRepository.findById(itemId).orElse(null);
        if (item != null) {
            Integer oldCount = item.getCount();
            item.setCount(newCount);
            inventoryItemRepository.save(item);

            // Create adjustment record
            InventoryAdjustment adjustment = new InventoryAdjustment(oldCount, newCount, reason, item, adjustedBy);
            inventoryAdjustmentRepository.save(adjustment);

            // Create alert if needed
            if (newCount < 10) {
                AlertSeverity severity = newCount == 0 ? AlertSeverity.CRITICAL : AlertSeverity.WARNING;
                AlertType type = newCount == 0 ? AlertType.ZERO_STOCK : AlertType.LOW_STOCK;

                alertService.createAlert(type, severity,
                        (newCount == 0 ? "Zero Stock: " : "Low Stock: ") + item.getName(),
                        "Stock adjusted to " + newCount + " units. Reason: " + reason,
                        item, item.getFranchise(), adjustedBy);
            }
        }
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
}