package com.intain.copilot.service;

import com.intain.copilot.model.LoanException;
import com.intain.copilot.model.LoanRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AiReviewService {

    @Value("${ai.api.key:}")
    private String apiKey;

    @Value("${ai.api.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent}")
    private String apiUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> generateExplanationAndFix(LoanRecord record, LoanException ex) {
        String prompt = String.format(
                "You are a loan verification audit assistant. " +
                        "A loan record failed validation with error: [%s: %s] on field [%s].\n" +
                        "Loan Details: Principal=$%s, Balance=$%s, Rate=%s, Status=%s, DPD=%s, Origination=%s, Maturity=%s.\n"
                        +
                        "Task: Explain why this is high risk and suggest a human remediation action.\n",
                ex.getExceptionCode(), ex.getDescription(), ex.getFieldName(),
                record.getOriginalPrincipal(), record.getCurrentBalance(), record.getInterestRate(),
                record.getPaymentStatus(), record.getDaysPastDue(), record.getOriginationDate(),
                record.getMaturityDate());

        Map<String, Object> responsePayload = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            if (apiKey == null || apiKey.trim().isEmpty()) {
                responsePayload.put("recommendation", "AI Analysis: Validation anomaly on '" + ex.getFieldName()
                        + "'. Review against servicer records and adjust accordingly.");
                responsePayload.put("model", "gemini-1.5-flash (local-simulated)");
                responsePayload.put("latency_ms", System.currentTimeMillis() - startTime);
                return responsePayload;
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = Map.of(
                    "contents", List.of(
                            Map.of("parts", List.of(Map.of("text", prompt)))));

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(apiUrl + "?key=" + apiKey, entity,
                    String.class);

            responsePayload.put("recommendation", response.getBody());
            responsePayload.put("model", "gemini-1.5-flash");
            responsePayload.put("latency_ms", System.currentTimeMillis() - startTime);
        } catch (Exception e) {
            responsePayload.put("recommendation",
                    "Review recommendation: Verify field '" + ex.getFieldName() + "' against manifest.");
            responsePayload.put("error", e.getMessage());
        }

        return responsePayload;
    }
}