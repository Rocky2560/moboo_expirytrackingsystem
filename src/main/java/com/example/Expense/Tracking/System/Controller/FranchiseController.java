package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Enum.ItemStatus;
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
@RequestMapping("/franchises")
public class FranchiseController {
    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private InventoryService inventoryService;

    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String franchisesDashboard(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null || !"ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/dashboard";
        }

        List<Franchise> franchises = franchiseService.getAllFranchises();
        model.addAttribute("franchises", franchises);

        // Add performance metrics for each franchise
        franchises.forEach(franchise -> {
            List<InventoryItem> items = inventoryService.getItemsByFranchise(franchise);
            long totalItems = items.size();
            long goodItems = items.stream()
                    .filter(item -> item.getStatus() == ItemStatus.GOOD)
                    .count();
            double healthScore = totalItems > 0 ? (double) goodItems / totalItems * 100 : 0;
            franchise.getInventoryItems().forEach(item -> {}); // Just to trigger lazy loading
        });

        model.addAttribute("totalFranchises", franchises.size());
        model.addAttribute("activeAlerts", alertService.getActiveAlertsCount());

        return "franchises";
    }

    @PostMapping("/add")
    public String addFranchise(@RequestParam String name,
                               @RequestParam String email,
                               @RequestParam String address,
                               HttpSession session) {
        Franchise franchise = new Franchise(name, email, address);
        franchiseService.saveFranchise(franchise);

        // Create user for the new franchise
        String userEmail = (String) session.getAttribute("user");
        User adminUser = userService.findByEmail(userEmail).orElse(null);
        if (adminUser != null) {
            User franchiseUser = new User(name, email, "franchise123", com.example.Expense.Tracking.System.Enum.UserRole.FRANCHISE);
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

        return "redirect:/franchises";
    }

    @GetMapping("/{id}")
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

        return "franchise-details";
    }

    @PostMapping("/{id}/update")
    public String updateFranchise(@PathVariable Long id,
                                  @RequestParam String name,
                                  @RequestParam String email,
                                  @RequestParam String address) {
        Franchise franchise = franchiseService.findById(id).orElse(null);
        if (franchise != null) {
            franchise.setName(name);
            franchise.setEmail(email);
            franchise.setAddress(address);
            franchiseService.saveFranchise(franchise);
        }

        return "redirect:/franchises/" + id;
    }
}