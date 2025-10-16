package com.example.Expense.Tracking.System;

import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ExpenseTrackingSystemApplication {
    @Autowired
    private static UserRepository userRepository; // ✅ Injected instance

	public static void main(String[] args) {

        User user = userRepository.findByUsername("admin").orElse(null);

        if(user != null) {
            boolean isAdmin = user.getRoles().stream()
                    .anyMatch(r -> r.getName().equals("ROLE_ADMIN"));
            System.out.println("Is Admin: " + isAdmin);
        }

//        SpringApplication.run(ExpenseTrackingSystemApplication.class, args);
	}



}
