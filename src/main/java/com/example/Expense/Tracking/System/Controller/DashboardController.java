package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Enum.ItemStatus;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Service.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

//        if (UserRole.ADMIN.name().equals(userRole)) {
            // Get admin's franchise from session
            Long adminFranchiseId = (Long) session.getAttribute("franchiseId");

            if (adminFranchiseId != null) {
                Franchise adminFranchise = franchiseService.findById(adminFranchiseId).orElse(null);
                if (adminFranchise != null) {
                    // Admin sees only items from their assigned franchise/shop
                    List<InventoryItem> shopItems = inventoryService.getItemsByFranchise(adminFranchise);

                    // Apply status filter
                    List<InventoryItem> filteredItems = filterItemsByStatus(shopItems, statusFilter);

                    model.addAttribute("franchises", franchiseService.getAllFranchises()); // Still show all franchises in UI for reference
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
                    // ✅ ADD PROPER AUTHORIZATION CHECK
                    String userEmailFromSession = (String) session.getAttribute("user");
                    User currentUser = userService.findByEmail(userEmailFromSession).orElse(null);

                    if (currentUser == null) {
                        // User doesn't have proper franchise association
                        return "redirect:/dashboard?error=unauthorized";
                    }

                    Franchise franchise = currentUser.getFranchise();

                    // ✅ NOW SAFE - we know this user belongs to this franchise
                    List<InventoryItem> franchiseItems = inventoryService.getItemsByFranchise(franchise);

                    // Apply status filter
                    List<InventoryItem> filteredItem = filterItemsByStatus(franchiseItems, statusFilter);

                    Long FranchiseId = (Long) session.getAttribute("franchiseId");

                    if (FranchiseId != null) {
                        Franchise Franchise = franchiseService.findById(FranchiseId).orElse(null);
                        if (Franchise != null) {
                            // Admin sees only items from their assigned franchise/shop
                            List<InventoryItem> shopItems = inventoryService.getItemsByFranchise(Franchise);

                            // Apply status filter
                            List<InventoryItem> filteredItems = filterItemsByStatus(shopItems, statusFilter);

                            model.addAttribute("inventoryItems", filteredItems);
                            model.addAttribute("totalItems", filteredItems.size());
                            model.addAttribute("currentFranchise", franchise); // Useful for UI

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
//                    }
            }
        }
        model.addAttribute("currentPage", "dashboard");
        return "dashboard";
    }

        private List<InventoryItem> filterItemsByStatus(List<InventoryItem> items, String statusFilter) {
            if (statusFilter == null || "ALL".equals(statusFilter)) {
                return items;
            }

            return items.stream().filter(item -> {
                ItemStatus itemStatus = item.getStatus();

                switch (statusFilter) {
                    case "GOOD":
                        return itemStatus == ItemStatus.GOOD;
                    case "LOW_STOCK":
                        return itemStatus == ItemStatus.LOW_STOCK;
                    case "EXPIRING_SOON":
                        return itemStatus == ItemStatus.EXPIRING_SOON;
                    case "EXPIRED":
                        return itemStatus == ItemStatus.EXPIRED;
                    default:
                        return true; // Return all items if filter doesn't match
                }
            }).collect(Collectors.toList());
        }
        }


