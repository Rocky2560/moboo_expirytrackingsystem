package com.example.Expense.Tracking.System.Controller;


import com.example.Expense.Tracking.System.Entity.Alert;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Service.AlertService;
import com.example.Expense.Tracking.System.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/alerts")
public class AlertController {
    @Autowired
    private AlertService alertService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String alertsDashboard(HttpSession session, Model model,
                                  @RequestParam(required = false) String severity) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null) {
            return "redirect:/dashboard";
        }

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);
        model.addAttribute("selectedSeverity", severity);

        List<Alert> alerts;
        if ("ADMIN".equals(userRole)) {
            if (severity != null && !severity.isEmpty()) {
                alerts = alertService.getAllActiveAlerts().stream()
                        .filter(alert -> alert.getSeverity().name().equals(severity))
                        .toList();
            } else {
                alerts = alertService.getAllActiveAlerts();
            }
        } else {
            Long franchiseId = (Long) session.getAttribute("franchiseId");
            var franchiseService = com.example.Expense.Tracking.System.Service.FranchiseService.class;
            // Get franchise and filter alerts
            alerts = alertService.getAllActiveAlerts().stream()
                    .filter(alert -> alert.getFranchise() != null &&
                            alert.getFranchise().getId().equals(franchiseId))
                    .filter(alert -> severity == null || severity.isEmpty() ||
                            alert.getSeverity().name().equals(severity))
                    .toList();
        }

        model.addAttribute("alerts", alerts);
        model.addAttribute("totalAlerts", alerts.size());
        model.addAttribute("criticalAlerts", alertService.getCriticalAlertsCount());
        model.addAttribute("warningAlerts", alertService.getWarningAlertsCount());

        return "alerts";
    }

    @PostMapping("/{id}/resolve")
    public String resolveAlert(@PathVariable Long id, HttpSession session) {
        String userEmail = (String) session.getAttribute("user");
        User user = userService.findByEmail(userEmail).orElse(null);

        if (user != null) {
            alertService.resolveAlert(id, user);
        }

        return "redirect:/alerts";
    }

    @PostMapping("/resolve-all")
    public String resolveAllAlerts(HttpSession session) {
        String userEmail = (String) session.getAttribute("user");
        User user = userService.findByEmail(userEmail).orElse(null);

        if (user != null) {
            List<Alert> alerts = alertService.getAllActiveAlerts();
            for (Alert alert : alerts) {
                alertService.resolveAlert(alert.getId(), user);
            }
        }

        return "redirect:/alerts";
    }
}
