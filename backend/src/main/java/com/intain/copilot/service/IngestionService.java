package com.intain.copilot.service;

import com.intain.copilot.model.AuditLog;
import com.intain.copilot.model.FileUpload;
import com.intain.copilot.model.LoanException;
import com.intain.copilot.model.LoanRecord;
import com.intain.copilot.repository.AuditLogRepository;
import com.intain.copilot.repository.FileUploadRepository;
import com.intain.copilot.repository.LoanExceptionRepository;
import com.intain.copilot.repository.LoanRecordRepository;
import com.opencsv.CSVReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class IngestionService {

    @Autowired
    private FileUploadRepository fileUploadRepo;
    @Autowired
    private LoanRecordRepository loanRecordRepo;
    @Autowired
    private LoanExceptionRepository exceptionRepo;
    @Autowired
    private AuditLogRepository auditLogRepo;
    @Autowired
    private ValidationEngine validationEngine;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public FileUpload processCsvUpload(MultipartFile file, String uploader) throws Exception {
        FileUpload upload = new FileUpload(file.getOriginalFilename(), uploader);
        upload = fileUploadRepo.save(upload);

        Set<String> existingLoanIds = new HashSet<>();
        List<LoanRecord> recordsToSave = new ArrayList<>();
        List<LoanException> exceptionsToSave = new ArrayList<>();

        int validCount = 0;
        int failedCount = 0;

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] header = reader.readNext(); // skip header
            String[] row;

            while ((row = reader.readNext()) != null) {
                if (row.length < 5)
                    continue;

                LoanRecord record = new LoanRecord();
                record.setFileUpload(upload);
                record.setSourceSystem("CSV_INGESTION");

                record.setLoanId(getVal(row, 0));
                record.setBorrowerId(getVal(row, 1));
                record.setLoanType(getVal(row, 2));
                record.setOriginationDate(parseDate(getVal(row, 3)));
                record.setMaturityDate(parseDate(getVal(row, 4)));
                record.setOriginalPrincipal(parseBigDecimal(getVal(row, 5)));
                record.setCurrentBalance(parseBigDecimal(getVal(row, 6)));
                record.setInterestRate(parseBigDecimal(getVal(row, 7)));
                record.setTermMonths(parseInt(getVal(row, 8)));
                record.setBorrowerState(getVal(row, 9));
                record.setLoanPurpose(getVal(row, 10));
                record.setCreditGrade(getVal(row, 11));
                record.setEmploymentLength(getVal(row, 12));
                record.setIncomeBand(getVal(row, 13));
                record.setPaymentStatus(getVal(row, 14));
                record.setDaysPastDue(parseInt(getVal(row, 15)));
                record.setServicerName(getVal(row, 16));
                record.setLastPaymentDate(parseDate(getVal(row, 17)));
                record.setDocumentStatus(getVal(row, 18));
                record.setLastUpdatedAt(LocalDateTime.now());

                List<LoanException> errors = validationEngine.validateRecord(record, existingLoanIds);

                if (errors.isEmpty()) {
                    record.setStatus("PENDING_VERIFICATION");
                    validCount++;
                } else {
                    record.setStatus("EXCEPTION");
                    failedCount++;
                    for (LoanException ex : errors) {
                        ex.setLoanRecord(record);
                        exceptionsToSave.add(ex);
                    }
                }

                if (record.getLoanId() != null && !record.getLoanId().isEmpty()) {
                    existingLoanIds.add(record.getLoanId());
                }

                recordsToSave.add(record);
            }
        }

        loanRecordRepo.saveAll(recordsToSave);
        exceptionRepo.saveAll(exceptionsToSave);

        upload.setTotalRecords(recordsToSave.size());
        upload.setValidRecords(validCount);
        upload.setFailedRecords(failedCount);
        fileUploadRepo.save(upload);

        auditLogRepo.save(new AuditLog(null, "FILE_UPLOADED", uploader,
                "Processed " + recordsToSave.size() + " records from " + file.getOriginalFilename() + ". Exceptions: "
                        + failedCount));

        return upload;
    }

    private String getVal(String[] row, int index) {
        return (index < row.length && row[index] != null) ? row[index].trim() : null;
    }

    private LocalDate parseDate(String val) {
        if (val == null || val.trim().isEmpty())
            return null;
        try {
            return LocalDate.parse(val.trim(), DATE_FORMAT);
        } catch (Exception e) {
            return null;
        }
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.trim().isEmpty())
            return null;
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseInt(String val) {
        if (val == null || val.trim().isEmpty())
            return 0;
        try {
            return Integer.parseInt(val.trim());
        } catch (Exception e) {
            return 0;
        }
    }
}