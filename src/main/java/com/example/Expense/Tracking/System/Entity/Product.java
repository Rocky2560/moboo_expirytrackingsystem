//package com.example.Expense.Tracking.System.Entity;
//import jakarta.persistence.*;
//import jakarta.validation.constraints.NotBlank;
//import java.util.List;
//
//@Entity
//@Table(name = "products")
//public class Product {
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @NotBlank
//    private String name;
//
//    @NotBlank
//    private String category;
//
//    @ManyToOne
//    @JoinColumn(name = "franchise_id", nullable = false)
//    private Franchise franchise;
//
//    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//    private List<InventoryBatch> batches;
//
//    // Constructors
//    public Product() {}
//
//    public Product(String name, String category, Franchise franchise) {
//        this.name = name;
//        this.category = category;
//        this.franchise = franchise;
//    }
//
//    // Getters and Setters
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getName() { return name; }
//    public void setName(String name) { this.name = name; }
//
//    public String getCategory() { return category; }
//    public void setCategory(String category) { this.category = category; }
//
//    public Franchise getFranchise() { return franchise; }
//    public void setFranchise(Franchise franchise) { this.franchise = franchise; }
//
//    public List<InventoryBatch> getBatches() { return batches; }
//    public void setBatches(List<InventoryBatch> batches) { this.batches = batches; }
//
//    // Calculate total quantity across all batches
//    public Integer getTotalQuantity() {
//        return batches != null ?
//                batches.stream().mapToInt(InventoryBatch::getQuantity).sum() : 0;
//    }
//
//    // Get overall status based on all batches
//    public ItemStatus getOverallStatus() {
//        if (batches == null || batches.isEmpty()) {
//            return ItemStatus.LOW_STOCK; // No stock
//        }
//
//        // Check if any batch is expired
//        boolean hasExpired = batches.stream()
//                .anyMatch(batch -> batch.getExpiryDate() != null &&
//                        batch.getExpiryDate().isBefore(LocalDate.now()));
//
//        if (hasExpired) {
//            return ItemStatus.EXPIRED;
//        }
//
//        // Check if any batch is expiring soon
//        boolean hasExpiringSoon = batches.stream()
//                .anyMatch(batch -> batch.getExpiryDate() != null &&
//                        batch.getExpiryDate().isBefore(LocalDate.now().plusDays(7)));
//
//        if (hasExpiringSoon) {
//            return ItemStatus.EXPIRING_SOON;
//        }
//
//        // Check total quantity for low stock
//        if (getTotalQuantity() < 10) {
//            return ItemStatus.LOW_STOCK;
//        }
//
//        return ItemStatus.GOOD;
//    }
//}