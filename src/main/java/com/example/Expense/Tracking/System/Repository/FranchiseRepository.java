package com.example.Expense.Tracking.System.Repository;


import com.example.Expense.Tracking.System.Entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

    @Repository
    public interface FranchiseRepository extends JpaRepository<Franchise, Long> {
    }