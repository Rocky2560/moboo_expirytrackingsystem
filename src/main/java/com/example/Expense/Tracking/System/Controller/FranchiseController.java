package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Enum.AlertType;
import com.example.Expense.Tracking.System.Enum.ItemStatus;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FranchiseController {
    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @GetMapping("/franchises")
    public String franchisesDashboard(HttpSession session, Model model,
                                      @RequestParam(required = false) Long franchiseId,
                                      @RequestParam(required = false) String statusFilter) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null || !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/dashboard";
        }

        // Get all franchises for the dropdown
        List<Franchise> allFranchises = franchiseService.getAllFranchises();
        model.addAttribute("franchises", allFranchises);
        model.addAttribute("selectedFranchiseId", franchiseId);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");

        List<InventoryItem> inventoryItems;
        Franchise selectedFranchise = null;

        if (franchiseId != null) {
            // Show inventory for specific franchise
            selectedFranchise = franchiseService.findById(franchiseId).orElse(null);
            if (selectedFranchise != null) {
                inventoryItems = inventoryService.getItemsByFranchise(selectedFranchise);
                model.addAttribute("selectedFranchise", selectedFranchise);
            } else {
                inventoryItems = List.of();
            }
        } else {
            // Show all inventory across all franchises
            inventoryItems = inventoryService.getAllItems();
        }

        // Apply status filter
        List<InventoryItem> filteredItems = filterItemsByStatus(inventoryItems, statusFilter);
        model.addAttribute("inventoryItems", filteredItems);
        model.addAttribute("totalItems", filteredItems.size());

        // Calculate stats for filtered items
        long expiredCount = filteredItems.stream()
                .filter(item -> item.getStatus() == ItemStatus.EXPIRED).count();
        long expiringSoonCount = filteredItems.stream()
                .filter(item -> item.getStatus() == ItemStatus.EXPIRING_SOON).count();
        long lowStockCount = filteredItems.stream()
                .filter(item -> item.getStatus() == ItemStatus.LOW_STOCK).count();

        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("expiringSoonCount", expiringSoonCount);
        model.addAttribute("lowStockCount", lowStockCount);

        // Check for critical low stock (below 5) and create alerts
        checkAndCreateLowStockAlerts(filteredItems);

        return "franchises";
    }

    // Method to check for items with stock below 5 and create alerts
    private void checkAndCreateLowStockAlerts(List<InventoryItem> items) {
        User adminUser = new User("System", "system@moboo.com", "system", UserRole.ADMIN);

        for (InventoryItem item : items) {
            // Check if stock is below 5 (critical low stock)
            if (item.getCount() != null && item.getCount() < 5 && item.getCount() >= 0) {
                // Check if alert already exists for this item
                boolean alertExists = alertService.alertExistsForItem(item, AlertType.LOW_STOCK_CRITICAL);

                if (!alertExists) {
                    String message = "Critical Low Stock: " + item.getName();
                    String description = "Stock level is " + item.getCount() + " units (below threshold of 5). Immediate action required.";

                    alertService.createAlert(
                            AlertType.LOW_STOCK_CRITICAL,
                            AlertSeverity.CRITICAL,
                            message,
                            description,
                            item,
                            item.getFranchise(),
                            adminUser
                    );
                }
            }
        }
    }

    private List<InventoryItem> filterItemsByStatus(List<InventoryItem> items, String statusFilter) {
        if (statusFilter == null || "ALL".equals(statusFilter)) {
            return items;
        }

        try {
            ItemStatus filterStatus = ItemStatus.valueOf(statusFilter);
            return items.stream()
                    .filter(item -> item.getStatus() == filterStatus)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return items;
        }
    }


    @PostMapping("franchises/add")
    public String addFranchise(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String password,
                               HttpSession session) {
        Franchise franchise = new Franchise(name, email);
        franchiseService.saveFranchise(franchise);

        // Create user for the new franchise
        String userEmail = (String) session.getAttribute("user");
        User adminUser = userService.findByEmail(userEmail).orElse(null);
        if (adminUser != null) {
            User franchiseUser = new User(name, email, password, com.example.Expense.Tracking.System.Enum.UserRole.FRANCHISE);
            franchiseUser.setFranchise(franchise);
            userService.saveUser(franchiseUser);

            // Create alert for new franchise
            alertService.createAlert(
                    com.example.Expense.Tracking.System.Enum.AlertType.NEW_FRANCHISE,
                    com.example.Expense.Tracking.System.Enum.AlertSeverity.INFO,
                    "New Franchise Added: " + name,
                    "New franchise " + name + " has been added to the system",
                    null, franchise, adminUser
            );
        }

        return "redirect:/dashboard?success=fadded";
    }

    @GetMapping("franchises/{id}")
    public String franchiseDetails(@PathVariable Long id, Model model, HttpSession session) {
        if (!"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/dashboard";
        }

        Franchise franchise = franchiseService.findById(id).orElse(null);
        if (franchise == null) {
            return "redirect:/franchises";
        }

        model.addAttribute("franchise", franchise);

        // Get inventory for this franchise
        List<InventoryItem> items = inventoryService.getItemsByFranchise(franchise);
        model.addAttribute("inventoryItems", items);
        model.addAttribute("totalItems", items.size());

        // Performance metrics
        long goodItems = items.stream()
                .filter(item -> item.getStatus() == com.example.Expense.Tracking.System.Enum.ItemStatus.GOOD)
                .count();
        double healthScore = items.size() > 0 ? (double) goodItems / items.size() * 100 : 0;
        model.addAttribute("healthScore", String.format("%.1f", healthScore));

        // Alerts for this franchise
        model.addAttribute("activeAlerts", alertService.getActiveAlertsCountByFranchise(franchise));

        return "franchise";
    }

    @PostMapping("franchises/{id}/update")
    public String updateFranchise(@PathVariable Long id,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String password) {
        Franchise franchise = franchiseService.findById(id).orElse(null);
        if (franchise != null) {
            franchise.setName(name);
            franchise.setEmail(email);
            franchise.setPassword(password);
            franchiseService.saveFranchise(franchise);
        }

        return "redirect:/franchises/" + id;
    }
}