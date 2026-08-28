package com.intain.copilot.repository;

import com.intain.copilot.model.LoanException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanExceptionRepository extends JpaRepository<LoanException, Long> {
    List<LoanException> findBySeverity(String severity);

    List<LoanException> findByStatus(String status);

    long countByStatus(String status);
}