package com.example.Expense.Tracking.System.Repository;

import com.example.Expense.Tracking.System.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
}
