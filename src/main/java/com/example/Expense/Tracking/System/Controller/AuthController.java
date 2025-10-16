package com.example.Expense.Tracking.System.Controller;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Repository.UserRepository;
import com.example.Expense.Tracking.System.Component.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public String login(@RequestBody User login) {
        Optional<User> userOpt = userRepository.findByUsername(login.getUsername());
        if(userOpt.isPresent()) {
            User user = userOpt.get();
            if(user.getPassword().equals(login.getPassword())) {
                return jwtUtil.generateToken(user.getUsername(), user.getRole().name());
            }
        }
        throw new RuntimeException("Invalid username or password");
    }
}

