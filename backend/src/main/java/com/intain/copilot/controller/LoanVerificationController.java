package com.intain.copilot.controller;

import com.intain.copilot.model.*;
import com.intain.copilot.repository.*;
import com.intain.copilot.service.*;
import com.intain.copilot.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LoanVerificationController {

    @Autowired
    private LoanRecordRepository loanRepo;
    @Autowired
    private LoanExceptionRepository exceptionRepo;
    @Autowired
    private VerifiedRecordRepository verifiedRepo;
    @Autowired
    private AuditLogRepository auditRepo;
    @Autowired
    private ValidationEngine validationEngine;
    @Autowired
    private AiReviewService aiService;
    @Autowired
    private IngestionService ingestionService;

    // POST /api/upload - Ingest and parse CSV loan tape
    @PostMapping("/upload")
    public ResponseEntity<?> uploadLoanTape(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "uploader", defaultValue = "operator@intain.com") String uploader) {
        try {
            FileUpload result = ingestionService.processCsvUpload(file, uploader);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // GET /api/summary - Dashboard KPIs
    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getSummary() {
        Map<String, Object> summary = new HashMap<>();
        long total = loanRepo.count();
        long openExceptions = exceptionRepo.countByStatus("OPEN");
        long verified = verifiedRepo.count();

        summary.put("totalLoans", total);
        summary.put("openExceptions", openExceptions);
        summary.put("verifiedRecords", verified);
        summary.put("dataQualityScore", calculateQualityScore(total, openExceptions));
        return ResponseEntity.ok(summary);
    }

    // GET /api/loans - All normalized loan records
    @GetMapping("/loans")
    public ResponseEntity<List<LoanRecord>> getAllLoans() {
        return ResponseEntity.ok(loanRepo.findAll());
    }

    // GET /api/loans/{id} - Single loan record
    @GetMapping("/loans/{id}")
    public ResponseEntity<LoanRecord> getLoanById(@PathVariable Long id) {
        return loanRepo.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/exceptions - Open validation exceptions
    @GetMapping("/exceptions")
    public ResponseEntity<List<LoanException>> getExceptions(@RequestParam(required = false) String severity) {
        if (severity != null && !severity.trim().isEmpty()) {
            return ResponseEntity.ok(exceptionRepo.findBySeverity(severity.toUpperCase()));
        }
        return ResponseEntity.ok(exceptionRepo.findByStatus("OPEN"));
    }

    // POST /api/exceptions/{id}/ai-assist - AI Copilot review recommendation
    @PostMapping("/exceptions/{id}/ai-assist")
    public ResponseEntity<Map<String, Object>> getAiAssistance(@PathVariable Long id) {
        LoanException ex = exceptionRepo.findById(id).orElseThrow();
        Map<String, Object> aiResult = aiService.generateExplanationAndFix(ex.getLoanRecord(), ex);

        ex.setAiRecommendation((String) aiResult.get("recommendation"));
        exceptionRepo.save(ex);

        logAudit(ex.getLoanRecord().getId(), "AI_RECOMMENDATION_GENERATED", "Reviewer", aiResult);
        return ResponseEntity.ok(aiResult);
    }

    // POST /api/loans/{id}/verify - Maker-Checker verification with SHA-256
    @PostMapping("/loans/{id}/verify")
    public ResponseEntity<?> verifyLoan(
            @PathVariable Long id,
            @RequestParam(value = "verifiedBy", defaultValue = "reviewer@intain.com") String verifiedBy) {
        LoanRecord record = loanRepo.findById(id).orElseThrow();

        String payloadToHash = record.getLoanId() + "|" + record.getCurrentBalance() + "|" + LocalDateTime.now() + "|"
                + verifiedBy;
        String recordHash = HashUtil.generateSha256(payloadToHash);

        VerifiedLoanRecord verified = new VerifiedLoanRecord();
        verified.setLoanRecord(record);
        verified.setCanonicalData(record.toString());
        verified.setSourceFile(record.getFileUpload());
        verified.setVerifiedBy(verifiedBy);
        verified.setRecordHash(recordHash);
        verifiedRepo.save(verified);

        record.setStatus("VERIFIED");
        loanRepo.save(record);

        // Resolve associated exceptions
        List<LoanException> openExceptions = exceptionRepo.findByStatus("OPEN");
        for (LoanException ex : openExceptions) {
            if (ex.getLoanRecord().getId().equals(record.getId())) {
                ex.setStatus("RESOLVED");
                exceptionRepo.save(ex);
            }
        }

        logAudit(record.getId(), "RECORD_VERIFIED", verifiedBy, Map.of("recordHash", recordHash));
        return ResponseEntity.ok(verified);
    }

    // GET /api/verified-loans - Verified records ledger
    @GetMapping("/verified-loans")
    public ResponseEntity<List<VerifiedLoanRecord>> getVerifiedLoans() {
        return ResponseEntity.ok(verifiedRepo.findAll());
    }

    // GET /api/audit/{loanId} - Full audit lineage
    @GetMapping("/audit/{loanId}")
    public ResponseEntity<List<AuditLog>> getAuditTrail(@PathVariable Long loanId) {
        return ResponseEntity.ok(auditRepo.findByLoanRecordIdOrderByTimestampDesc(loanId));
    }

    private void logAudit(Long loanId, String action, String user, Map<String, Object> details) {
        AuditLog log = new AuditLog();
        log.setLoanRecordId(loanId);
        log.setActionType(action);
        log.setPerformedBy(user);
        log.setDetails(details.toString());
        log.setTimestamp(LocalDateTime.now());
        auditRepo.save(log);
    }

    private double calculateQualityScore(long total, long exceptions) {
        if (total == 0)
            return 100.0;
        double score = (1.0 - ((double) exceptions / (total * 2))) * 100.0;
        return Math.max(0.0, Math.round(score * 10.0) / 10.0);
    }
}