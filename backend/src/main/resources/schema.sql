-- 1. Raw File Ingestion Lineage
CREATE TABLE IF NOT EXISTS file_uploads (
    id BIGSERIAL PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    uploaded_by VARCHAR(100) NOT NULL,
    total_records INT DEFAULT 0,
    valid_records INT DEFAULT 0,
    failed_records INT DEFAULT 0,
    upload_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Working Loan Records
CREATE TABLE IF NOT EXISTS loan_records (
    id BIGSERIAL PRIMARY KEY,
    file_upload_id BIGINT REFERENCES file_uploads(id) ON DELETE CASCADE,
    loan_id VARCHAR(100),
    borrower_id VARCHAR(100),
    loan_type VARCHAR(50),
    origination_date DATE,
    maturity_date DATE,
    original_principal NUMERIC(15,2),
    current_balance NUMERIC(15,2),
    interest_rate NUMERIC(6,4),
    term_months INT,
    borrower_state VARCHAR(10),
    loan_purpose VARCHAR(100),
    credit_grade VARCHAR(10),
    employment_length VARCHAR(50),
    income_band VARCHAR(50),
    payment_status VARCHAR(50),
    days_past_due INT,
    servicer_name VARCHAR(100),
    last_payment_date DATE,
    last_updated_at TIMESTAMP,
    document_status VARCHAR(50),
    source_system VARCHAR(50),
    status VARCHAR(30) DEFAULT 'PENDING_VALIDATION'
);

-- 3. Loan Exception Queue
CREATE TABLE IF NOT EXISTS loan_exceptions (
    id BIGSERIAL PRIMARY KEY,
    loan_record_id BIGINT REFERENCES loan_records(id) ON DELETE CASCADE,
    exception_code VARCHAR(100) NOT NULL,
    field_name VARCHAR(100),
    severity VARCHAR(20) NOT NULL,
    description TEXT NOT NULL,
    status VARCHAR(30) DEFAULT 'OPEN',
    ai_recommendation TEXT,
    reviewer_comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Verified Loan Ledger (Immutable)
CREATE TABLE IF NOT EXISTS verified_loan_records (
    id BIGSERIAL PRIMARY KEY,
    loan_record_id BIGINT UNIQUE REFERENCES loan_records(id) ON DELETE CASCADE,
    canonical_data TEXT NOT NULL,
    source_file_id BIGINT REFERENCES file_uploads(id),
    verified_by VARCHAR(100) NOT NULL,
    verified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    record_hash VARCHAR(64) NOT NULL
);

-- 5. Audit Trail Lineage
CREATE TABLE IF NOT EXISTS audit_logs (
    id BIGSERIAL PRIMARY KEY,
    loan_record_id BIGINT,
    action_type VARCHAR(50) NOT NULL,
    performed_by VARCHAR(100) NOT NULL,
    details TEXT,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);