package com.example.Expense.Tracking.System.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name="items")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;
    private int quantity;
    private LocalDate expiryDate;

    @ManyToOne
    @JoinColumn(name="franchise_id")
    private User franchise;
}
