package com.example.Expense.Tracking.System.Repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.example.Expense.Tracking.System.Entity.Item;

public interface ItemRepository extends JpaRepository<Item, Long> {
}
