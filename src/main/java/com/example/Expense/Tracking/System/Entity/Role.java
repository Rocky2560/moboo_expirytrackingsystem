//package com.example.Expense.Tracking.System.Entity;
//
//import jakarta.persistence.*;
//import lombok.Getter;
//import lombok.NoArgsConstructor;
//import lombok.Setter;
//
//import java.util.HashSet;
//import java.util.Set;
//
//@Entity
//@Table(name = "roles")
//@Getter
//@Setter
//public class Role {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(unique = true)
//    private String name; // e.g., "ROLE_ADMIN" or "ROLE_USER"
//
//    @ManyToMany(mappedBy = "roles")
//    private Set<User> users = new HashSet<>();
//    // getters and setters
//}
