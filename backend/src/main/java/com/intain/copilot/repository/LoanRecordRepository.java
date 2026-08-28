package com.intain.copilot.repository;

import com.intain.copilot.model.LoanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LoanRecordRepository extends JpaRepository<LoanRecord, Long> {
    Optional<LoanRecord> findByLoanId(String loanId);

    List<LoanRecord> findByStatus(String status);
}