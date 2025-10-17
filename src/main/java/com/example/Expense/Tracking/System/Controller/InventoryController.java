package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Entity.InventoryAdjustment;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Service.EmailService;
import com.example.Expense.Tracking.System.Service.FranchiseService;
import com.example.Expense.Tracking.System.Service.InventoryService;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
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

        if ("ADMIN".equals(userRole)) {
            List<Franchise> franchises = franchiseService.getAllFranchises();
            model.addAttribute("franchises", franchises);

            // Get all items with filtering
            List<InventoryItem> allItems = inventoryService.searchItems(search, category, null);
            model.addAttribute("inventoryItems", allItems);
            model.addAttribute("totalItems", allItems.size());

            // Stats
            model.addAttribute("lowStockCount",
                    allItems.stream().filter(item -> item.getCount() < 10).count());
            model.addAttribute("expiringSoonCount",
                    allItems.stream().filter(item ->
                            item.getExpiryDate().isBefore(LocalDate.now().plusDays(7)) &&
                                    !item.getExpiryDate().isBefore(LocalDate.now())).count());
            model.addAttribute("expiredCount",
                    allItems.stream().filter(item -> item.getExpiryDate().isBefore(LocalDate.now())).count());

        } else {
            Long franchiseId = (Long) session.getAttribute("franchiseId");
            Franchise franchise = franchiseService.findById(franchiseId).orElse(null);

            if (franchise != null) {
                // ✅ ONLY ONE assignment - use the filtered results
                List<InventoryItem> franchiseItems = inventoryService.searchItems(search, category, franchise);
                model.addAttribute("inventoryItems", franchiseItems);
                model.addAttribute("totalItems", franchiseItems.size());

                // Stats
                model.addAttribute("lowStockCount",
                        franchiseItems.stream().filter(item -> item.getCount() < 10).count());
                model.addAttribute("expiringSoonCount",
                        franchiseItems.stream().filter(item ->
                                item.getExpiryDate().isBefore(LocalDate.now().plusDays(7)) &&
                                        !item.getExpiryDate().isBefore(LocalDate.now())).count());
                model.addAttribute("expiredCount",
                        franchiseItems.stream().filter(item -> item.getExpiryDate().isBefore(LocalDate.now())).count());
            }
            // ❌ REMOVE these duplicate lines:
            // List<InventoryItem> inventoryItems = inventoryService.getItemsByFranchise(franchise);
            // model.addAttribute("inventoryItems", inventoryItems);
        }

        // Add categories for filter dropdown
        model.addAttribute("categories", inventoryService.getAllCategories());


        return "inventory";
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

        return "redirect:/dashboard";
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
                               @RequestParam String email,
                               @RequestParam String address) {
        Franchise franchise = new Franchise(name, email, address);
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