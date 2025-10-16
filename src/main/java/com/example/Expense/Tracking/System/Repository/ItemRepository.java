package com.example.Expense.Tracking.System.Repository;

import com.example.Expense.Tracking.System.Entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByFranchiseId(Long franchiseId);
}

