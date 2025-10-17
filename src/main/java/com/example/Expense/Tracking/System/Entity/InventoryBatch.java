//package com.example.Expense.Tracking.System.Entity;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.Min;
//import java.time.LocalDate;
//
//@Entity
//@Table(name = "inventory_batches")
//public class InventoryBatch {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Min(0)
//    private Integer quantity;
//
//    private LocalDate expiryDate;
//
//    private LocalDate receivedDate; // When this batch was received
//
//    @ManyToOne
//    @JoinColumn(name = "product_id", nullable = false)
//    private Product product;
//
//    // Constructors
//    public InventoryBatch() {}
//
//    public InventoryBatch(Integer quantity, LocalDate expiryDate, Product product) {
//        this.quantity = quantity;
//        this.expiryDate = expiryDate;
//        this.receivedDate = LocalDate.now();
//        this.product = product;
//    }
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public Integer getQuantity() { return quantity; }
//    public void setQuantity(Integer quantity) { this.quantity = quantity; }
//
//    public LocalDate getExpiryDate() { return expiryDate; }
//    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
//
//    public LocalDate getReceivedDate() { return receivedDate; }
//    public void setReceivedDate(LocalDate receivedDate) { this.receivedDate = receivedDate; }
//
//    public Product getProduct() { return product; }
//    public void setProduct(Product product) { this.product = product; }
//
//    // Get status for this specific batch
//    public ItemStatus getBatchStatus() {
//        if (expiryDate == null) {
//            return quantity < 10 ? ItemStatus.LOW_STOCK : ItemStatus.GOOD;
//        }
//
//        LocalDate today = LocalDate.now();
//        if (expiryDate.isBefore(today)) {
//            return ItemStatus.EXPIRED;
//        } else if (expiryDate.isBefore(today.plusDays(7))) {
//            return ItemStatus.EXPIRING_SOON;
//        } else {
//            return quantity < 10 ? ItemStatus.LOW_STOCK : ItemStatus.GOOD;
//        }
//    }
//}