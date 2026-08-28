package com.intain.copilot.repository;

import com.intain.copilot.model.VerifiedLoanRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VerifiedRecordRepository extends JpaRepository<VerifiedLoanRecord, Long> {
}