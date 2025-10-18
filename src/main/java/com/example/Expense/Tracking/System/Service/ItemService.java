package com.example.Expense.Tracking.System.Service;

import com.example.Expense.Tracking.System.Entity.Item;
import com.example.Expense.Tracking.System.Repository.ItemRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ItemService {

    private final ItemRepository repo;

    public ItemService(ItemRepository repo) {
        this.repo = repo;
    }

    public List<Item> getAllItems() {
        return repo.findAll();
    }

    public void saveItem(Item item) {
        repo.save(item);
    }

    public Item getItemById(Long id) {
        Optional<Item> opt = repo.findById(id);
        return opt.orElse(null);
    }

    public void deleteItem(Long id) {
        repo.deleteById(id);
    }
}
