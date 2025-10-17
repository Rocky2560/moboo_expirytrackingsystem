package com.example.Expense.Tracking.System.Repository;


import com.example.Expense.Tracking.System.Entity.Alert;
import com.example.Expense.Tracking.System.Enum.AlertSeverity;
import com.example.Expense.Tracking.System.Entity.Franchise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByFranchiseAndResolved(Franchise franchise, boolean resolved);
    List<Alert> findByResolved(boolean resolved);
    List<Alert> findBySeverityAndResolved(AlertSeverity severity, boolean resolved);

    @Query("SELECT a FROM Alert a WHERE a.franchise = :franchise AND a.createdAt BETWEEN :startDate AND :endDate")
    List<Alert> findByFranchiseAndDateRange(@Param("franchise") Franchise franchise,
                                            @Param("startDate") LocalDateTime startDate,
                                            @Param("endDate") LocalDateTime endDate);

    List<Alert> findByFranchise(Franchise franchise);

    Optional<Alert> findById(Long id);

    long countByFranchiseAndResolved(Franchise franchise, boolean resolved);
    long countByResolved(boolean resolved);
    long countBySeverityAndResolved(AlertSeverity severity, boolean resolved);
}