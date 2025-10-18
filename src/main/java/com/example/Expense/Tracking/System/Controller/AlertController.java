package com.example.Expense.Tracking.System.Controller;


import com.example.Expense.Tracking.System.Entity.*;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Controller

public class AlertController {

    @Autowired
    private AlertService alertService;

    @Autowired
    private FranchiseService franchiseService;

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;

    @GetMapping("/alerts")
    public String alertsDashboard(HttpSession session, Model model,
                                  @RequestParam(required = false) String statusFilter,
                                  @RequestParam(required = false) Boolean includeResolved,
                                  @RequestParam(required = false) Long franchiseId) {
        String userEmail = (String) session.getAttribute("user");
        if (userEmail == null) {
            return "redirect:/dashboard";
        }

        String userRole = (String) session.getAttribute("userRole");
        model.addAttribute("userRole", userRole);
        model.addAttribute("selectedStatus", statusFilter != null ? statusFilter : "ALL");
        model.addAttribute("includeResolved", includeResolved != null && includeResolved);
        model.addAttribute("selectedFranchiseId", franchiseId);

        List<Alert> alerts;
        Franchise selectedFranchise = null;

        if ("ADMIN".equals(userRole)) {
            List<Franchise> allFranchises = franchiseService.getAllFranchises();
            model.addAttribute("franchises", allFranchises);

            // Admin can see all franchises' alerts or filter by specific franchise
            if (franchiseId != null && franchiseId > 0) {
                selectedFranchise = franchiseService.findById(franchiseId).orElse(null);
                if (selectedFranchise != null) {
                    alerts = alertService.getAlertsWithNotificationStatus(selectedFranchise, includeResolved != null && includeResolved);
                    model.addAttribute("selectedFranchise", selectedFranchise);
                } else {
                    // If franchise not found, show all alerts (or empty list - your choice)
                    alerts = alertService.getAlertsWithNotificationStatus(null, includeResolved != null && includeResolved);
                }
            } else {
                // Show alerts from ALL franchises when no specific franchise is selected
                alerts = alertService.getAlertsWithNotificationStatus(null, includeResolved != null && includeResolved);
            }
        } else {
            // Franchise users can only see their own franchise alerts
            Long userFranchiseId = (Long) session.getAttribute("franchiseId");
            if (userFranchiseId != null && userFranchiseId > 0) {
                Franchise userFranchise = franchiseService.findById(userFranchiseId).orElse(null);
                if (userFranchise != null) {
                    alerts = alertService.getAlertsWithNotificationStatus(userFranchise, includeResolved != null && includeResolved);
                    model.addAttribute("selectedFranchise", userFranchise);
                } else {
                    alerts = List.of();
                }
            } else {
                alerts = List.of();
            }
        }

        // Apply status filter
        List<Alert> filteredAlerts = filterAlertsByStatus(alerts, statusFilter);
        model.addAttribute("alerts", filteredAlerts);
        model.addAttribute("totalAlerts", filteredAlerts.size());

        // Stats
        long criticalAlerts = filteredAlerts.stream()
                .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL).count();
        long warningAlerts = filteredAlerts.stream()
                .filter(alert -> alert.getSeverity() == AlertSeverity.WARNING).count();
        long sentNotifications = filteredAlerts.stream()
                .filter(Alert::isNotificationSent).count();
        long failedNotifications = filteredAlerts.stream()
                .filter(Alert::isNotificationFailed).count();

        model.addAttribute("criticalAlerts", criticalAlerts);
        model.addAttribute("warningAlerts", warningAlerts);
        model.addAttribute("sentNotifications", sentNotifications);
        model.addAttribute("failedNotifications", failedNotifications);

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

    @PostMapping("/retry/{id}")
    public String retryNotification(@PathVariable Long id, HttpSession session) {
        Alert alert = alertService.getAlertById(id);
        if (alert != null && alert.isNotificationFailed()) {
            // Reset retry count and attempt to send again
            alert.setNotificationRetryCount(0);
            alert.setNotificationFailed(false);
            alert.setNotificationErrorMessage(null);
            alertService.updateAlert(alert);
            emailService.sendAlertEmail(alert);
        }
        return "redirect:/alerts";
    }

    private List<Alert> filterAlertsByStatus(List<Alert> alerts, String statusFilter) {
        if (statusFilter == null || "ALL".equals(statusFilter)) {
            return alerts;
        }

        switch (statusFilter) {
            case "SENT":
                return alerts.stream().filter(Alert::isNotificationSent).collect(Collectors.toList());
            case "FAILED":
                return alerts.stream().filter(Alert::isNotificationFailed).collect(Collectors.toList());
            case "PENDING":
                return alerts.stream()
                        .filter(alert -> !alert.isNotificationSent() && !alert.isNotificationFailed())
                        .collect(Collectors.toList());
            case "CRITICAL":
                return alerts.stream()
                        .filter(alert -> alert.getSeverity() == AlertSeverity.CRITICAL)
                        .collect(Collectors.toList());
            case "WARNING":
                return alerts.stream()
                        .filter(alert -> alert.getSeverity() == AlertSeverity.WARNING)
                        .collect(Collectors.toList());
            default:
                return alerts;
        }
    }
}