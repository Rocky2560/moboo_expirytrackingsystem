package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Service.*;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Controller
public class DashboardController {
    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private UserService userService;

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null) {
            return "dashboard"; // This will show login modal
        }

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);

        if (UserRole.ADMIN.name().equals(userRole)) {
            // Admin view - show all franchises and items
            List<Franchise> franchises = franchiseService.getAllFranchises();
            List<InventoryItem> allItems = inventoryService.getAllItems();

            model.addAttribute("franchises", franchises);
            model.addAttribute("inventoryItems", allItems);
            model.addAttribute("totalItems", inventoryService.getTotalItems());

            // Calculate stats for all franchises combined
            int expiredCount = 0;
            int expiringSoonCount = 0;
            int lowStockCount = 0;

            for (Franchise franchise : franchises) {
                expiredCount += inventoryService.getExpiredItems(franchise).size();
                expiringSoonCount += inventoryService.getExpiringSoonItems(franchise).size();
                lowStockCount += inventoryService.getLowStockItems(franchise).size();
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
                model.addAttribute("inventoryItems", franchiseItems);
                model.addAttribute("totalItems", franchiseItems.size());
                model.addAttribute("expiredCount", inventoryService.getExpiredItems(franchise).size());
                model.addAttribute("expiringSoonCount", inventoryService.getExpiringSoonItems(franchise).size());
                model.addAttribute("lowStockCount", inventoryService.getLowStockItems(franchise).size());
            }
        }

        model.addAttribute("currentPage", "dashboard");
        return "dashboard";
    }

//    @GetMapping("/inventory")
//    public String redirectToInventory() {
//        return "redirect:/inventory";
//    }
//
//    @GetMapping("/franchises")
//    public String redirectToFranchises(HttpSession session) {
//        if ("ADMIN".equals(session.getAttribute("userRole"))) {
//            return "redirect:/franchises";
//        }
//        return "redirect:/dashboard";
//    }
//
//    @GetMapping("/alerts")
//    public String redirectToAlerts() {
//        return "redirect:/alerts";
//    }
}