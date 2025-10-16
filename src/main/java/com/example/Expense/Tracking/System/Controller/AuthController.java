package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLogin() {
        return "redirect:/dashboard";
    }

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session,
                        Model model) {
        if (userService.validateCredentials(email, password)) {
            User user = userService.findByEmail(email).orElse(null);
            if (user != null) {
                session.setAttribute("user", user.getEmail());
                session.setAttribute("userName", user.getName());
                session.setAttribute("userRole", user.getRole().name());
                if (user.getRole() == UserRole.FRANCHISE) {
                    session.setAttribute("franchiseId", user.getFranchise().getId());
                }
                return "redirect:/dashboard";
            }
        }
        model.addAttribute("error", "Invalid credentials");
        return "redirect:/dashboard?error=true";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/dashboard";
    }
}

