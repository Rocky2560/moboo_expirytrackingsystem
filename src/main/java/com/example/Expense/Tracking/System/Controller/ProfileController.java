package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // Show profile page
    @GetMapping
    public String showProfile(Model model, HttpSession session) {
        // Get user details from session
        String userEmail = (String) session.getAttribute("userEmail");
        String userName = (String) session.getAttribute("userName");
        String userRole = (String) session.getAttribute("userRole");
        Long userId = (Long) session.getAttribute("userId");
        System.out.println(userId + userName + userRole +userEmail);

        // ✅ Check if user is logged in (session attributes should be set during login)
        if (userEmail == null || userId == null) {
            // User is not properly logged in, redirect to login
            return "redirect:/login";
        }

        // Add user data to model (don't reset session attributes here!)
        model.addAttribute("userEmail", userEmail);
        model.addAttribute("userName", userName != null ? userName : userEmail);
        model.addAttribute("userRole", userRole != null ? userRole : "USER");

        return "profile";
    }

    // Update email
    @PostMapping("/update-email")
    public String updateEmail(@RequestParam("newEmail") @Email String newEmail,
                              @RequestParam("currentPassword") @NotBlank String password,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {

        // ✅ Critical: Check if user is logged in
        String currentEmail = (String) session.getAttribute("userEmail");
        Long userId = (Long) session.getAttribute("userId");

        if (currentEmail == null || userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please login again.");
            return "redirect:/login";
        }

        System.out.println("User ID: " + userId + ", Current Email: " + currentEmail);
        System.out.println("New Email: " + newEmail);

        try {
            // Validate current password
            if (!userService.validatePassword(currentEmail, password)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
                return "redirect:/profile";
            }

            // Check if new email already exists
            if (userService.emailExists(newEmail)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Email already exists.");
                return "redirect:/profile";
            }

            // Update email
            userService.updateEmail(userId, newEmail);

            // Update session with new email
            session.setAttribute("userEmail", newEmail);

            redirectAttributes.addFlashAttribute("successMessage", "Email updated successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            System.err.println("Error in updateEmail: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred. Please try again.");
            return "redirect:/profile";
        }
    }

    // Update password
    @PostMapping("/update-password")
    public String updatePassword(@RequestParam @NotBlank String currentPassword,
                                 @RequestParam @Size(min = 8) String newPassword,
                                 @RequestParam @NotBlank String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {

        // ✅ Critical: Check if user is logged in
        String userEmail = (String) session.getAttribute("userEmail");
        Long userId = (Long) session.getAttribute("userId");

        if (userEmail == null || userId == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Session expired. Please login again.");
            return "redirect:/login";
        }

        try {
            // Validate current password
            if (!userService.validatePassword(userEmail, currentPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
                return "redirect:/profile";
            }

            // Check if new passwords match
            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Passwords do not match.");
                return "redirect:/profile";
            }

            // Validate password strength
            if (!isValidPassword(newPassword)) {
                redirectAttributes.addFlashAttribute("errorMessage", "Password must contain at least 8 characters, including uppercase, lowercase, number, and special character.");
                return "redirect:/profile";
            }

            // Update password
            userService.updatePassword(userId, passwordEncoder.encode(newPassword));

            redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully!");
            return "redirect:/profile";

        } catch (Exception e) {
            System.err.println("Error in updatePassword: " + e.getMessage());
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred. Please try again.");
            return "redirect:/profile";
        }
    }

    // Helper method to validate password strength
    private boolean isValidPassword(String password) {
        if (password == null) return false;

        // At least 8 characters
        if (password.length() < 8) return false;

        // At least one uppercase letter
        if (!password.matches(".*[A-Z].*")) return false;

        // At least one lowercase letter
        if (!password.matches(".*[a-z].*")) return false;

        // At least one digit
        if (!password.matches(".*\\d.*")) return false;

        // At least one special character
        if (!password.matches(".*[^A-Za-z0-9].*")) return false;

        return true;
    }
}