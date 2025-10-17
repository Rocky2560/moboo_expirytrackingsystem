package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Config.SecurityConfig;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Optional;

@Controller
public class AuthController {
    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLogin() {
        return "redirect:/dashboard";
    }

    @Autowired
    private SecurityConfig securityConfig;

    @PostMapping("/login")
    public String login(@RequestParam String email,
                        @RequestParam String password,
                        HttpSession session) {

        Optional<User> optionalUser = userService.findByEmail(email);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            if (securityConfig.passwordEncoder().matches(password, user.getPassword())) {
                // Set session attributes
                session.setAttribute("user", user.getEmail());
                session.setAttribute("userName", user.getName());
                session.setAttribute("userRole", user.getRole().name());

                if (user.getRole() == UserRole.FRANCHISE && user.getFranchise() != null) {
                    session.setAttribute("franchiseId", user.getFranchise().getId());
                }
                // Set SecurityContext so Spring Security knows user is authenticated
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + user.getRole().name())
                );
                Authentication auth = new UsernamePasswordAuthenticationToken(user.getEmail(), null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                return "redirect:/dashboard";
            }
        }

        // Invalid login
        return "redirect:/login?error=true";
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/dashboard";
    }
}

