# AI Development & Agentic Coding Log

This document records the agentic AI coding tools, prompts, validation procedures, and human architectural reviews applied throughout the development of the Loan Data Verification Copilot.

---

## 1. Tools Used
- **Primary AI Assistants:** Gemini 1.5 Flash, VS Code Copilot Agent
- **Frameworks & Libraries:** Spring Boot 3, Spring Data JPA, OpenCSV, Bootstrap 5

---

## 2. Representative Prompts & Use Cases

1. **System Architecture & Database Design:**
   > *"Design a canonical relational schema for a loan verification engine with separate tables for raw uploads, normalized working records, validation exceptions, immutable verified records, and audit logs."*
2. **Modular Validation Engine:**
   > *"Write a Java service to validate loan records against 8 distinct edge cases: maturity date precedence, negative balances, balance exceeding principal, interest rate range bounds, DPD vs status mismatch, closed loan balance anomalies, missing documents, and invalid US state postal abbreviations."*
3. **AI Copilot Endpoint:**
   > *"Generate a Spring Boot service that constructs structured prompt payloads to explain loan validation exceptions and recommend exact remediation values without direct data mutation."*
4. **Cryptographic Immutability:**
   > *"Implement a SHA-256 digest generator that hashes canonical loan payloads concatenated with review timestamps and reviewer IDs to prevent tampering."*
5. **Interactive Frontend:**
   > *"Build a Bootstrap 5 UI supporting role-based perspectives (Data Operator, Reviewer, Data Consumer) with live modal-driven exception resolution."*

---

## 3. Human Review & Verification Process
- All AI-generated code was compiled and unit-tested against real CSV edge cases.
- Every API endpoint was tested locally to guarantee strict status code compliance (`200 OK`, `400 Bad Request`).
- Database constraints and JPA relationships were manually checked to prevent N+1 query overhead and memory leaks.

---

## 4. Examples of Rejected AI Output

* **Rejected Item 1 (Direct Unchecked Mutation):** The initial AI suggestion proposed an automatic database update trigger for flagged loans.
  * *Why Rejected:* Violated the core hackathon requirement that AI output must not silently modify records without explicit reviewer oversight.
  * *Resolution:* Converted the AI output into a suggestion payload displayed in the Reviewer UI for human confirmation.

* **Rejected Item 2 (Incomplete Hash Digest):** The AI proposed hashing only the `loan_id` string for verification.
  * *Why Rejected:* Fails cryptographic auditability because modifications to the loan amount or status would produce identical hashes.
  * *Resolution:* Refactored the hashing logic to digest the entire canonical record state (`loanId|currentBalance|timestamp|verifiedBy`).

---

## 5. Summary Metrics
- **Estimated AI-Assisted Code:** ~65%
- **Human Engineering & Architecture:** ~35%