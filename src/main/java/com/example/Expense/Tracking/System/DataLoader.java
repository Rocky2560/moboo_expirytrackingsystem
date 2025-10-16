package com.example.Expense.Tracking.System;

import com.example.Expense.Tracking.System.Entity.Role;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Repository.RoleRepository;
import com.example.Expense.Tracking.System.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataLoader implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role adminRole = roleRepository.save(new Role("ROLE_ADMIN"));
            Role userRole = roleRepository.save(new Role("ROLE_USER"));

            User admin = new User("admin", passwordEncoder.encode("admin123"), Set.of(adminRole));
            User franchise = new User("franchise", passwordEncoder.encode("user123"), Set.of(userRole));

            userRepository.save(admin);
            userRepository.save(franchise);

            System.out.println("Default users created: admin/admin123, franchise/user123");
        }
    }
}
