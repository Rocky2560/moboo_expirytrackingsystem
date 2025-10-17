//package com.example.Expense.Tracking.System.Repository;
//
//
//import com.example.Expense.Tracking.System.Entity.Franchise;
//import com.example.Expense.Tracking.System.Entity.InventoryBatch;
//import com.example.Expense.Tracking.System.Entity.Product;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//import org.springframework.stereotype.Repository;
//import java.time.LocalDate;
//import java.util.List;
//
//@Repository
//public interface InventoryBatchRepository extends JpaRepository<InventoryBatch, Long> {
//    List<InventoryBatch> findByProduct(Product product);
//
//    @Query("SELECT b FROM InventoryBatch b WHERE b.product.franchise = :franchise")
//    List<InventoryBatch> findByFranchise(@Param("franchise") Franchise franchise);
//
//    @Query("SELECT b FROM InventoryBatch b WHERE b.product.franchise = :franchise AND b.expiryDate < :today")
//    List<InventoryBatch> findExpiredBatches(@Param("franchise") Franchise franchise, @Param("today") LocalDate today);
//
//    @Query("SELECT b FROM InventoryBatch b WHERE b.product.franchise = :franchise AND b.expiryDate BETWEEN :today AND :weekLater")
//    List<InventoryBatch> findExpiringSoonBatches(@Param("franchise") Franchise franchise,
//                                                 @Param("today") LocalDate today,
//                                                 @Param("weekLater") LocalDate weekLater);
//}
