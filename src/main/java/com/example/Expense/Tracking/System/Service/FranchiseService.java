package com.example.Expense.Tracking.System.Service;


import com.example.Expense.Tracking.System.Entity.Franchise;
import com.example.Expense.Tracking.System.Repository.FranchiseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class FranchiseService {
    @Autowired
    private FranchiseRepository franchiseRepository;

    public List<Franchise> getAllFranchises() {
        return franchiseRepository.findAll();
    }

    public Franchise saveFranchise(Franchise franchise) {
        return franchiseRepository.save(franchise);
    }

    public Optional<Franchise> findById(Long id) {
        return franchiseRepository.findById(id);
    }
}