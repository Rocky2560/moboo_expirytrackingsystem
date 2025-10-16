package com.example.Expense.Tracking.System.Entity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;
import com.example.Expense.Tracking.System.Enum.ItemStatus;

@Entity
@Table(name = "inventory_items")
public class InventoryItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String category;

    @Min(0)
    private Integer count;

    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name = "franchise_id", nullable = false)
    private Franchise franchise;

    // Constructors
    public InventoryItem() {}

    public InventoryItem(String name, String category, Integer count, LocalDate expiryDate, Franchise franchise) {
        this.name = name;
        this.category = category;
        this.count = count;
        this.expiryDate = expiryDate;
        this.franchise = franchise;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Franchise getFranchise() { return franchise; }
    public void setFranchise(Franchise franchise) { this.franchise = franchise; }

    // Status calculation method
    public ItemStatus getStatus() {
        LocalDate today = LocalDate.now();

        if (expiryDate.isBefore(today)) {
            return ItemStatus.EXPIRED;
        } else if (expiryDate.isBefore(today.plusDays(7))) {
            return ItemStatus.EXPIRING_SOON;
        } else if (count < 10) {
            return ItemStatus.LOW_STOCK;
        } else {
            return ItemStatus.GOOD;
        }
    }
}
