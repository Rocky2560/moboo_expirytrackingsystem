package com.example.Expense.Tracking.System.Controller;

import com.example.Expense.Tracking.System.Entity.Item;
import com.example.Expense.Tracking.System.Entity.User;
import com.example.Expense.Tracking.System.Repository.ItemRepository;
import com.example.Expense.Tracking.System.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/add/{franchiseId}")
    public Item addItem(@PathVariable Long franchiseId, @RequestBody Item item) {
        Optional<User> userOpt = userRepository.findById(franchiseId);
        if(userOpt.isPresent()) {
            item.setFranchise(userOpt.get());
            return itemRepository.save(item);
        }
        throw new RuntimeException("Franchise not found");
    }

    @GetMapping("/all")
    public List<Item> getAllItems() {
        return itemRepository.findAll();
    }

    @GetMapping("/franchise/{franchiseId}")
    public List<Item> getItemsByFranchise(@PathVariable Long franchiseId) {
        return itemRepository.findByFranchiseId(franchiseId);
    }
}
