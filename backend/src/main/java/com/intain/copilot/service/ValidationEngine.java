package com.intain.copilot.service;

import com.intain.copilot.model.LoanException;
import com.intain.copilot.model.LoanRecord;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class ValidationEngine {

    private static final Set<String> VALID_US_STATES = Set.of(
            "AL", "AK", "AZ", "AR", "CA", "CO", "CT", "DE", "FL", "GA", "HI", "ID", "IL", "IN", "IA",
            "KS", "KY", "LA", "ME", "MD", "MA", "MI", "MN", "MS", "MO", "MT", "NE", "NV", "NH", "NJ",
            "NM", "NY", "NC", "ND", "OH", "OK", "OR", "PA", "RI", "SC", "SD", "TN", "TX", "UT", "VT",
            "VA", "WA", "WV", "WI", "WY");

    public List<LoanException> validateRecord(LoanRecord record, Set<String> existingLoanIds) {
        List<LoanException> exceptions = new ArrayList<>();

        // 1. Missing or Duplicate Loan ID
        if (record.getLoanId() == null || record.getLoanId().trim().isEmpty()) {
            exceptions.add(
                    createException(record, "MISSING_LOAN_ID", "loanId", "CRITICAL", "Loan ID is missing or empty."));
        } else if (existingLoanIds.contains(record.getLoanId())) {
            exceptions.add(createException(record, "DUPLICATE_LOAN_ID", "loanId", "CRITICAL",
                    "Duplicate Loan ID detected: " + record.getLoanId()));
        }

        // 2. Dates Validation: Maturity before Origination
        if (record.getOriginationDate() != null && record.getMaturityDate() != null) {
            if (record.getMaturityDate().isBefore(record.getOriginationDate())) {
                exceptions.add(createException(record, "INVALID_MATURITY_DATE", "maturityDate", "HIGH",
                        "Maturity date cannot be prior to origination date."));
            }
        }

        // 3. Negative Principal or Balances
        if (record.getOriginalPrincipal() != null && record.getOriginalPrincipal().compareTo(BigDecimal.ZERO) < 0) {
            exceptions.add(createException(record, "NEGATIVE_PRINCIPAL", "originalPrincipal", "HIGH",
                    "Original principal balance cannot be negative."));
        }
        if (record.getCurrentBalance() != null && record.getCurrentBalance().compareTo(BigDecimal.ZERO) < 0) {
            exceptions.add(createException(record, "NEGATIVE_BALANCE", "currentBalance", "HIGH",
                    "Current balance cannot be negative."));
        }

        // 4. Current Balance > Original Principal
        if (record.getCurrentBalance() != null && record.getOriginalPrincipal() != null) {
            if (record.getCurrentBalance().compareTo(record.getOriginalPrincipal()) > 0) {
                exceptions.add(createException(record, "BALANCE_EXCEEDS_PRINCIPAL", "currentBalance", "HIGH",
                        "Current balance exceeds original loan principal."));
            }
        }

        // 5. Interest Rate Range (0.0% to 35.0%)
        if (record.getInterestRate() != null) {
            if (record.getInterestRate().compareTo(BigDecimal.ZERO) < 0
                    || record.getInterestRate().compareTo(new BigDecimal("0.35")) > 0) {
                exceptions.add(createException(record, "INVALID_INTEREST_RATE", "interestRate", "MEDIUM",
                        "Interest rate is outside standard operational thresholds (0% - 35%)."));
            }
        }

        // 6. Payment Status vs Days Past Due Inconsistency
        if (record.getDaysPastDue() != null && record.getDaysPastDue() > 30) {
            if ("CURRENT".equalsIgnoreCase(record.getPaymentStatus())) {
                exceptions.add(createException(record, "STATUS_DPD_MISMATCH", "paymentStatus", "HIGH",
                        "Payment status is marked 'CURRENT' but Days Past Due is " + record.getDaysPastDue()));
            }
        }

        // 7. Closed Loan with Positive Balance
        if ("CLOSED".equalsIgnoreCase(record.getPaymentStatus())
                || "PAID_OFF".equalsIgnoreCase(record.getPaymentStatus())) {
            if (record.getCurrentBalance() != null && record.getCurrentBalance().compareTo(BigDecimal.ZERO) > 0) {
                exceptions.add(createException(record, "CLOSED_WITH_BALANCE", "currentBalance", "HIGH",
                        "Loan marked as closed/paid-off but still retains a positive balance."));
            }
        }

        // 8. Missing Document Status
        if (record.getDocumentStatus() == null || record.getDocumentStatus().trim().isEmpty()) {
            exceptions.add(createException(record, "MISSING_DOCUMENTS", "documentStatus", "MEDIUM",
                    "Required document verification flag is missing."));
        }

        // 9. State Code Validation
        if (record.getBorrowerState() != null && !VALID_US_STATES.contains(record.getBorrowerState().toUpperCase())) {
            exceptions.add(createException(record, "INVALID_STATE_CODE", "borrowerState", "LOW",
                    "State code '" + record.getBorrowerState() + "' is not a valid US postal abbreviation."));
        }

        return exceptions;
    }

    private LoanException createException(LoanRecord record, String code, String field, String severity, String desc) {
        LoanException ex = new LoanException();
        ex.setLoanRecord(record);
        ex.setExceptionCode(code);
        ex.setFieldName(field);
        ex.setSeverity(severity);
        ex.setDescription(desc);
        ex.setStatus("OPEN");
        return ex;
    }
}