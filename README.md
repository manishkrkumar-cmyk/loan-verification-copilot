# Loan Data Verification Copilot

An AI-assisted full-stack console that ingests messy loan records, performs automated anomaly and compliance validation, utilizes LLM-driven intelligence for exception remediation, and generates a cryptographically hashed, immutable verified ledger.

Built for the **Intain Campus FinTech Challenge 2026 – Full Stack Track**.

---

## Key Features

- **Data Ingestion Engine:** Uploads, parses, and normalizes unstructured CSV loan tapes with full source-lineage preservation.
- **Rule-Based Validation Engine:** Detects 9+ edge-case anomalies including negative balances, maturity date conflicts, status/DPD mismatches, duplicate IDs, and invalid state postal codes.
- **AI Review Assistant (Copilot):** Provides plain-English root-cause explanations and suggested value remediation for flagged exceptions without silent data corruption.
- **Maker-Checker Workflow:** Enforces role-based operational separation between Data Operators, Reviewers, and Data Consumers.
- **Cryptographic Traceability:** Generates SHA-256 digests for all verified records and tracks complete lifecycle audit logs.

---

## Tech Stack

- **Backend:** Java 17, Spring Boot 3, Spring Data JPA, OpenCSV, H2 Database (In-Memory) / PostgreSQL
- **Frontend:** HTML5, CSS3, JavaScript (ES6+), Bootstrap 5
- **AI Integration:** Google Gemini REST API / OpenAI API

---

## Test Credentials & Roles

| Role | Email / Identifier | Permissions |
| :--- | :--- | :--- |
| **Data Operator** | `operator@intain.com` | Ingest CSV loan tapes, view upload summaries and parsing metrics. |
| **Reviewer** | `reviewer@intain.com` | Access exception queue, trigger AI analysis, edit/override values, approve/reject records. |
| **Data Consumer** | `consumer@intain.com` | View immutable verified ledger, inspect SHA-256 hashes, and review audit trails. |

---

## Instructions to Run Locally

### 1. Prerequisites
- Java JDK 17 or higher
- Maven 3.8+
- Modern Web Browser

### 2. Backend Setup
```bash
# Navigate to backend directory
cd backend

# Build and run the Spring Boot application
mvn clean spring-boot:run