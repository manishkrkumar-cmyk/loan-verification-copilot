package com.intain.copilot.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "verified_loan_records")
public class VerifiedLoanRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "loan_record_id", unique = true)
    private LoanRecord loanRecord;

    @Column(columnDefinition = "TEXT")
    private String canonicalData;

    @ManyToOne
    @JoinColumn(name = "source_file_id")
    private FileUpload sourceFile;

    private String verifiedBy;
    private LocalDateTime verifiedAt = LocalDateTime.now();
    private String recordHash;

    public VerifiedLoanRecord() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LoanRecord getLoanRecord() {
        return loanRecord;
    }

    public void setLoanRecord(LoanRecord loanRecord) {
        this.loanRecord = loanRecord;
    }

    public String getCanonicalData() {
        return canonicalData;
    }

    public void setCanonicalData(String canonicalData) {
        this.canonicalData = canonicalData;
    }

    public FileUpload getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(FileUpload sourceFile) {
        this.sourceFile = sourceFile;
    }

    public String getVerifiedBy() {
        return verifiedBy;
    }

    public void setVerifiedBy(String verifiedBy) {
        this.verifiedBy = verifiedBy;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public String getRecordHash() {
        return recordHash;
    }

    public void setRecordHash(String recordHash) {
        this.recordHash = recordHash;
    }
}