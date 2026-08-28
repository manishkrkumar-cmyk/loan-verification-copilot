package com.intain.copilot.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "loan_records")
public class LoanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "file_upload_id")
    private FileUpload fileUpload;

    private String loanId;
    private String borrowerId;
    private String loanType;
    private LocalDate originationDate;
    private LocalDate maturityDate;
    private BigDecimal originalPrincipal;
    private BigDecimal currentBalance;
    private BigDecimal interestRate;
    private Integer termMonths;
    private String borrowerState;
    private String loanPurpose;
    private String creditGrade;
    private String employmentLength;
    private String incomeBand;
    private String paymentStatus;
    private Integer daysPastDue;
    private String servicerName;
    private LocalDate lastPaymentDate;
    private LocalDateTime lastUpdatedAt;
    private String documentStatus;
    private String sourceSystem;
    private String status = "PENDING_VALIDATION";

    public LoanRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public void setFileUpload(FileUpload fileUpload) {
        this.fileUpload = fileUpload;
    }

    public String getLoanId() {
        return loanId;
    }

    public void setLoanId(String loanId) {
        this.loanId = loanId;
    }

    public String getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(String borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getLoanType() {
        return loanType;
    }

    public void setLoanType(String loanType) {
        this.loanType = loanType;
    }

    public LocalDate getOriginationDate() {
        return originationDate;
    }

    public void setOriginationDate(LocalDate originationDate) {
        this.originationDate = originationDate;
    }

    public LocalDate getMaturityDate() {
        return maturityDate;
    }

    public void setMaturityDate(LocalDate maturityDate) {
        this.maturityDate = maturityDate;
    }

    public BigDecimal getOriginalPrincipal() {
        return originalPrincipal;
    }

    public void setOriginalPrincipal(BigDecimal originalPrincipal) {
        this.originalPrincipal = originalPrincipal;
    }

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public BigDecimal getInterestRate() {
        return interestRate;
    }

    public void setInterestRate(BigDecimal interestRate) {
        this.interestRate = interestRate;
    }

    public Integer getTermMonths() {
        return termMonths;
    }

    public void setTermMonths(Integer termMonths) {
        this.termMonths = termMonths;
    }

    public String getBorrowerState() {
        return borrowerState;
    }

    public void setBorrowerState(String borrowerState) {
        this.borrowerState = borrowerState;
    }

    public String getLoanPurpose() {
        return loanPurpose;
    }

    public void setLoanPurpose(String loanPurpose) {
        this.loanPurpose = loanPurpose;
    }

    public String getCreditGrade() {
        return creditGrade;
    }

    public void setCreditGrade(String creditGrade) {
        this.creditGrade = creditGrade;
    }

    public String getEmploymentLength() {
        return employmentLength;
    }

    public void setEmploymentLength(String employmentLength) {
        this.employmentLength = employmentLength;
    }

    public String getIncomeBand() {
        return incomeBand;
    }

    public void setIncomeBand(String incomeBand) {
        this.incomeBand = incomeBand;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public Integer getDaysPastDue() {
        return daysPastDue;
    }

    public void setDaysPastDue(Integer daysPastDue) {
        this.daysPastDue = daysPastDue;
    }

    public String getServicerName() {
        return servicerName;
    }

    public void setServicerName(String servicerName) {
        this.servicerName = servicerName;
    }

    public LocalDate getLastPaymentDate() {
        return lastPaymentDate;
    }

    public void setLastPaymentDate(LocalDate lastPaymentDate) {
        this.lastPaymentDate = lastPaymentDate;
    }

    public LocalDateTime getLastUpdatedAt() {
        return lastUpdatedAt;
    }

    public void setLastUpdatedAt(LocalDateTime lastUpdatedAt) {
        this.lastUpdatedAt = lastUpdatedAt;
    }

    public String getDocumentStatus() {
        return documentStatus;
    }

    public void setDocumentStatus(String documentStatus) {
        this.documentStatus = documentStatus;
    }

    public String getSourceSystem() {
        return sourceSystem;
    }

    public void setSourceSystem(String sourceSystem) {
        this.sourceSystem = sourceSystem;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "{" +
                "loanId='" + loanId + '\'' +
                ", borrowerId='" + borrowerId + '\'' +
                ", loanType='" + loanType + '\'' +
                ", originationDate=" + originationDate +
                ", maturityDate=" + maturityDate +
                ", principal=" + originalPrincipal +
                ", balance=" + currentBalance +
                ", rate=" + interestRate +
                ", term=" + termMonths +
                ", state='" + borrowerState + '\'' +
                ", purpose='" + loanPurpose + '\'' +
                ", creditGrade='" + creditGrade + '\'' +
                ", status='" + paymentStatus + '\'' +
                ", dpd=" + daysPastDue +
                ", servicer='" + servicerName + '\'' +
                ", docStatus='" + documentStatus + '\'' +
                '}';
    }
}