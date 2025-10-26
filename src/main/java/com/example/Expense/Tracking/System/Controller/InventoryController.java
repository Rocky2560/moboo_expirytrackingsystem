package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.*;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Enum.AlertType;
import com.example.Expense.Tracking.System.Service.EmailService;
import com.example.Expense.Tracking.System.Service.FranchiseService;
import com.example.Expense.Tracking.System.Service.InventoryService;
import com.example.Expense.Tracking.System.Service.*;


import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/inventory")
public class InventoryController {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private EmailService emailService;


    @Autowired
    private UserService userService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private ItemService service;

    public InventoryController (ItemService itemService)
    {
        this.service = itemService;
    }

//    @GetMapping
//    public String inventoryDashboard(HttpSession session, Model model,
//                                     @RequestParam(required = false) String search,
//                                     @RequestParam(required = false) String category) {
//        String userEmail = (String) session.getAttribute("user");
//        if (userEmail == null) {
//            return "redirect:/dashboard";
//        }
//
//        String userRole = (String) session.getAttribute("userRole");
//        model.addAttribute("userRole", userRole);
//        model.addAttribute("searchTerm", search != null ? search : "");
//        model.addAttribute("selectedCategory", category != null ? category : "All");
//
//        if ("ADMIN".equals(userRole)) {
//            List<Franchise> franchises = franchiseService.getAllFranchises();
//            model.addAttribute("franchises", franchises);
//
//            // Get all items with filtering
//            List<InventoryItem> allItems = inventoryService.searchItems(search, category, null);
//            model.addAttribute("inventoryItems", allItems);
//            model.addAttribute("totalItems", allItems.size());
//
//            // Stats
//            model.addAttribute("lowStockCount",
//                    allItems.stream().filter(item -> item.getCount() < 10).count());
//            model.addAttribute("expiringSoonCount",
//                    allItems.stream().filter(item ->
//                            item.getExpiryDate().isBefore(LocalDate.now().plusDays(7)) &&
//                                    !item.getExpiryDate().isBefore(LocalDate.now())).count());
//            model.addAttribute("expiredCount",
//                    allItems.stream().filter(item -> item.getExpiryDate().isBefore(LocalDate.now())).count());
//
//        } else {
//            Long franchiseId = (Long) session.getAttribute("franchiseId");
//            Franchise franchise = franchiseService.findById(franchiseId).orElse(null);
//
//            if (franchise != null) {
//                // ✅ ONLY ONE assignment - use the filtered results
//                List<InventoryItem> franchiseItems = inventoryService.searchItems(search, category, franchise);
//                model.addAttribute("inventoryItems", franchiseItems);
//                model.addAttribute("totalItems", franchiseItems.size());
//
//                // Stats
//                model.addAttribute("lowStockCount",
//                        franchiseItems.stream().filter(item -> item.getCount() < 10).count());
//                model.addAttribute("expiringSoonCount",
//                        franchiseItems.stream().filter(item ->
//                                item.getExpiryDate().isBefore(LocalDate.now().plusDays(7)) &&
//                                        !item.getExpiryDate().isBefore(LocalDate.now())).count());
//                model.addAttribute("expiredCount",
//                        franchiseItems.stream().filter(item -> item.getExpiryDate().isBefore(LocalDate.now())).count());
//            }
//            // ❌ REMOVE these duplicate lines:
//            // List<InventoryItem> inventoryItems = inventoryService.getItemsByFranchise(franchise);
//            // model.addAttribute("inventoryItems", inventoryItems);
//        }
//
//        // Add categories for filter dropdown
//        model.addAttribute("listItems", service.getAllItems());
//        model.addAttribute("categories", inventoryService.getAllCategories());
//
//
//        return "inventory";
//    }
@GetMapping
public String inventoryDashboard(HttpSession session, Model model,
                                 @RequestParam(required = false) String search,
                                 @RequestParam(required = false) String category) {
    String userEmail = (String) session.getAttribute("user");
    if (userEmail == null) {
        return "redirect:/dashboard";
    }

    String userRole = (String) session.getAttribute("userRole");
    model.addAttribute("userRole", userRole);
    model.addAttribute("searchTerm", search != null ? search : "");
    model.addAttribute("selectedCategory", category != null ? category : "All");

    // Get the current user's franchise ID (works for both ADMIN and FRANCHISE)
    Long currentFranchiseId = (Long) session.getAttribute("franchiseId");

    if (currentFranchiseId == null) {
        // Handle case where user has no franchise assigned
        model.addAttribute("inventoryItems", List.of());
        model.addAttribute("totalItems", 0);
        model.addAttribute("lowStockCount", 0);
        model.addAttribute("expiringSoonCount", 0);
        model.addAttribute("expiredCount", 0);
    } else {
        Franchise currentFranchise = franchiseService.findById(currentFranchiseId).orElse(null);

        if (currentFranchise != null) {
            // Both ADMIN and FRANCHISE users get items from their respective franchise only
            List<InventoryItem> shopItems = inventoryService.searchItems(search, category, currentFranchise);
            model.addAttribute("inventoryItems", shopItems);
            model.addAttribute("totalItems", shopItems.size());

            // Stats filtered by the current shop/franchise only
            model.addAttribute("lowStockCount",
                    shopItems.stream().filter(item -> item.getCount() < 10).count());
            model.addAttribute("expiringSoonCount",
                    shopItems.stream().filter(item ->
                            item.getExpiryDate().isBefore(LocalDate.now().plusDays(7)) &&
                                    !item.getExpiryDate().isBefore(LocalDate.now())).count());
            model.addAttribute("expiredCount",
                    shopItems.stream().filter(item -> item.getExpiryDate().isBefore(LocalDate.now())).count());
        } else {
            // Franchise not found
            model.addAttribute("inventoryItems", List.of());
            model.addAttribute("totalItems", 0);
            model.addAttribute("lowStockCount", 0);
            model.addAttribute("expiringSoonCount", 0);
            model.addAttribute("expiredCount", 0);
        }
    }

    // Add categories for filter dropdown
    model.addAttribute("categories", inventoryService.getAllCategories());

    // Only show franchises list to ADMIN for reference (not for filtering items)
//    if ("ADMIN".equals(userRole)) {
        List<Franchise> franchises = franchiseService.getAllFranchises();
        model.addAttribute("franchises", franchises);
//    }
    model.addAttribute("listItems", service.getAllItems());
    return "inventory";
}

    @PostMapping("items/save")
    public String saveItem(@ModelAttribute("item") Item item) {
        service.saveItem(item);
        return "redirect:/inventory?success=added";
    }

    @PostMapping("items/update")
    public String updateItem(@RequestParam("itemId") Long id, @ModelAttribute("item") Item item) {
        Item existing = service.getItemById(id);
        existing.setName(item.getName());
        existing.setCategory(item.getCategory());
        service.saveItem(existing);
        return "redirect:/inventory?success=updated";
    }

    @PostMapping("item/delete")
    public String deleteItem(@RequestParam("id") Long id) {
        service.deleteItem(id);
        return "redirect:/inventory?success=deleted";
    }

    @PostMapping("/items/edit")
    public String editItem(@RequestParam Long id,
                           @RequestParam String name,
                           @RequestParam String category,
                           @RequestParam Integer count,
                           @RequestParam LocalDate expiryDate,
                           @RequestParam(required = false) Long franchiseId,
                           HttpSession session) {

        String userRole = (String) session.getAttribute("userRole");
        InventoryItem existingItem = inventoryService.findById(id).orElse(null);

        if (existingItem != null) {
            // For franchise users, ensure they can only edit their own items
            if ("FRANCHISE".equals(userRole)) {
                Long userFranchiseId = (Long) session.getAttribute("franchiseId");
                if (!existingItem.getFranchise().getId().equals(userFranchiseId)) {
                    return "redirect:/dashboard?error=unauthorized";
                }
            }

            // Update the item
            existingItem.setName(name);
            existingItem.setCategory(category);
            existingItem.setCount(count);
            existingItem.setExpiryDate(expiryDate);

            // Update franchise only if admin
            if ("ADMIN".equals(userRole) && franchiseId != null) {
                Franchise newFranchise = franchiseService.findById(franchiseId).orElse(null);
                if (newFranchise != null) {
                    existingItem.setFranchise(newFranchise);
                }
            }

            inventoryService.saveItem(existingItem);
            return "redirect:/dashboard?success=updated"; // Add success parameter
        }

        return "redirect:/dashboard?error=update_failed";
    }


    @PostMapping("/items/delete")
    public String deleteItem(@RequestParam Long id, HttpSession session) {
        String userRole = (String) session.getAttribute("userRole");
        InventoryItem itemToDelete = inventoryService.findById(id).orElse(null);

        if (itemToDelete != null) {
            // For franchise users, ensure they can only delete their own items
            if ("FRANCHISE".equals(userRole)) {
                Long userFranchiseId = (Long) session.getAttribute("franchiseId");
                if (!itemToDelete.getFranchise().getId().equals(userFranchiseId)) {
                    return "redirect:/dashboard?error=unauthorized";
                }
            }

            // Create alert for deletion
            String userEmail = (String) session.getAttribute("user");
            User user = userService.findByEmail(userEmail).orElse(null);
            if (user != null) {
                alertService.createAlert(
                        AlertType.INVENTORY_UPDATED,
                        AlertSeverity.INFO,
                        "Item Deleted: " + itemToDelete.getName(),
                        "Item '" + itemToDelete.getName() + "' was deleted by " + user.getName(),
                        null, itemToDelete.getFranchise(), user
                );
            }

            inventoryService.deleteItem(id);
            return "redirect:/dashboard?success=deleted"; // Add success parameter
        }

        return "redirect:/dashboard?error=item_not_found";
    }

    @PostMapping("/items/add")
    public String addItem(@RequestParam String name,
                          @RequestParam String category,
                          @RequestParam Integer count,
                          @RequestParam LocalDate expiryDate,
                          @RequestParam(required = false) Long franchiseId,
                          HttpSession session) {

        String userRole = (String) session.getAttribute("userRole");
        Franchise franchise;
        System.out.println(franchiseId);

        if ("ADMIN".equals(userRole) && franchiseId != null) {
            franchise = franchiseService.findById(franchiseId).orElse(null);
        } else {
            Long userFranchiseId = (Long) session.getAttribute("franchiseId");
            franchise = franchiseService.findById(userFranchiseId).orElse(null);
        }

        if (franchise != null) {
            InventoryItem item = new InventoryItem(name, category, count, expiryDate, franchise);
            inventoryService.saveItem(item);
        }

        return "redirect:/dashboard?success=added";
    }
    @PostMapping("/adjust")
    public String adjustItem(@RequestParam Long itemId,
                             @RequestParam Integer newCount,
                             @RequestParam String reason,
                             HttpSession session) {
        String userEmail = (String) session.getAttribute("user");
        User user = userService.findByEmail(userEmail).orElse(null);

        if (user != null) {
            inventoryService.adjustItemCount(itemId, newCount, reason, user);
        }

        return "redirect:/inventory";
    }

    @PostMapping("/franchises/add")
    public String addFranchise(@RequestParam String name,
                               @RequestParam String email) {
        Franchise franchise = new Franchise(name, email);
        franchiseService.saveFranchise(franchise);
        return "redirect:/dashboard";
    }

    @GetMapping("/adjustments/{itemId}")
    public String getAdjustmentHistory(@PathVariable Long itemId, Model model, HttpSession session) {
        List<InventoryAdjustment> adjustments = inventoryService.getAdjustmentHistory(itemId);
        model.addAttribute("adjustments", adjustments);
        return "fragments/adjustment-history :: adjustmentHistory";
    }
}