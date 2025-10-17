package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Enum.ItemStatus;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Service.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Controller
public class DashboardController {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model,
                            @RequestParam(required = false) String statusFilter) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null) {
            return "dashboard"; // This will show login modal
        }

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");

        if (UserRole.ADMIN.name().equals(userRole)) {
            // Admin view - show all franchises and items
            List<Franchise> franchises = franchiseService.getAllFranchises();
            List<InventoryItem> allItems = inventoryService.getAllItems();

            // Apply status filter
            List<InventoryItem> filteredItems = filterItemsByStatus(allItems, statusFilter);

            model.addAttribute("franchises", franchises);
            model.addAttribute("inventoryItems", filteredItems);
            model.addAttribute("totalItems", filteredItems.size());

            // Calculate stats for filtered items only
            int expiredCount = 0;
            int expiringSoonCount = 0;
            int lowStockCount = 0;

            for (InventoryItem item : filteredItems) {
                ItemStatus status = item.getStatus();
                if (status == ItemStatus.EXPIRED) {
                    expiredCount++;
                } else if (status == ItemStatus.EXPIRING_SOON) {
                    expiringSoonCount++;
                } else if (status == ItemStatus.LOW_STOCK) {
                    lowStockCount++;
                }
            }

            model.addAttribute("expiredCount", expiredCount);
            model.addAttribute("expiringSoonCount", expiringSoonCount);
            model.addAttribute("lowStockCount", lowStockCount);

        } else {
            // Franchise view - show only their items
            Long franchiseId = (Long) session.getAttribute("franchiseId");
            Franchise franchise = franchiseService.findById(franchiseId).orElse(null);

            if (franchise != null) {
                List<InventoryItem> franchiseItems = inventoryService.getItemsByFranchise(franchise);

                // Apply status filter
                List<InventoryItem> filteredItems = filterItemsByStatus(franchiseItems, statusFilter);

                model.addAttribute("inventoryItems", filteredItems);
                model.addAttribute("totalItems", filteredItems.size());

                // Calculate stats for filtered items
                int expiredCount = 0;
                int expiringSoonCount = 0;
                int lowStockCount = 0;

                for (InventoryItem item : filteredItems) {
                    ItemStatus status = item.getStatus();
                    if (status == ItemStatus.EXPIRED) {
                        expiredCount++;
                    } else if (status == ItemStatus.EXPIRING_SOON) {
                        expiringSoonCount++;
                    } else if (status == ItemStatus.LOW_STOCK) {
                        lowStockCount++;
                    }
                }

                model.addAttribute("expiredCount", expiredCount);
                model.addAttribute("expiringSoonCount", expiringSoonCount);
                model.addAttribute("lowStockCount", lowStockCount);
            }
        }

        model.addAttribute("currentPage", "dashboard");
        return "dashboard";
    }

    // Helper method to filter items by status
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
            return items; // Invalid status, return all items
        }
    }
}