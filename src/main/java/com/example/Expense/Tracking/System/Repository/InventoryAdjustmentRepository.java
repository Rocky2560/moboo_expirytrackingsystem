package com.example.Expense.Tracking.System.Repository;


import com.example.Expense.Tracking.System.Entity.InventoryAdjustment;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface InventoryAdjustmentRepository extends JpaRepository<InventoryAdjustment, Long> {
    List<InventoryAdjustment> findByInventoryItem(InventoryItem inventoryItem);
}