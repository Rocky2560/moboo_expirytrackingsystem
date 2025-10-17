package com.example.Expense.Tracking.System.Config;
import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Enum.UserRole;
import com.example.Expense.Tracking.System.Entity.InventoryItem;
import com.example.Expense.Tracking.System.Service.UserService;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Repository.FranchiseRepository;
import com.example.Expense.Tracking.System.Repository.InventoryItemRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;

@Component
public class DataInitializer implements CommandLineRunner {
    @Autowired
    private UserService userService;

    @Autowired
    private FranchiseRepository franchiseRepository;

    @Autowired
    private InventoryItemRepository inventoryItemRepository;

    @Override
    public void run(String... args) throws Exception {
        // Create franchises
        Franchise centralLondon = franchiseRepository.save(
                new Franchise("Moboo Central London", "central@moboo.com")
        );

        Franchise manchester = franchiseRepository.save(
                new Franchise("Moboo Manchester", "manchester@moboo.com")
        );

        Franchise birmingham = franchiseRepository.save(
                new Franchise("Moboo Birmingham", "birmingham@moboo.com")
        );

        // Create users
        userService.saveUser(new User("Admin User", "admin@moboo.com", "admin123", UserRole.ADMIN));
        userService.saveUser(new User("Central London", "central@moboo.com", "franchise123", UserRole.FRANCHISE));
        userService.saveUser(new User("Manchester", "manchester@moboo.com", "franchise123", UserRole.FRANCHISE));
        userService.saveUser(new User("Birmingham", "birmingham@moboo.com", "franchise123", UserRole.FRANCHISE));

        // Create inventory items
        inventoryItemRepository.saveAll(Arrays.asList(
                new InventoryItem("Tapioca Pearls", "Ingredients", 25, LocalDate.of(2024, 1, 15), centralLondon),
                new InventoryItem("Milk Tea Base", "Ingredients", 8, LocalDate.of(2024, 1, 20), centralLondon),
                new InventoryItem("Strawberry Syrup", "Syrups", 15, LocalDate.of(2024, 2, 10), centralLondon),
                new InventoryItem("Tapioca Pearls", "Ingredients", 3, LocalDate.of(2024, 1, 10), manchester),
                new InventoryItem("Green Tea Powder", "Ingredients", 12, LocalDate.of(2024, 3, 15), manchester),
                new InventoryItem("Mango Puree", "Fruits", 7, LocalDate.of(2024, 1, 25), birmingham)
        ));
    }
}
