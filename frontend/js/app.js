const API_BASE = 'http://localhost:8085/api';

let selectedExceptionId = null;
let selectedLoanId = null;
let copilotModalInstance = null;
let auditModalInstance = null;

document.addEventListener('DOMContentLoaded', () => {
  copilotModalInstance = new bootstrap.Modal(document.getElementById('copilotModal'));
  auditModalInstance = new bootstrap.Modal(document.getElementById('auditModal'));
  
  document.getElementById('roleSelector').addEventListener('change', handleRoleChange);
  handleRoleChange();
  fetchSummary();
});

function handleRoleChange() {
  const role = document.getElementById('roleSelector').value;
  document.querySelectorAll('.role-view').forEach(el => {
    el.classList.remove('active');
    el.style.opacity = '0';
  });
  
  const targetId = role === 'OPERATOR' ? 'view-operator' :
                   role === 'REVIEWER' ? 'view-reviewer' : 'view-consumer';
  
  const targetEl = document.getElementById(targetId);
  targetEl.classList.add('active');
  setTimeout(() => {
    targetEl.style.transition = 'opacity 0.25s ease-in-out';
    targetEl.style.opacity = '1';
  }, 10);

  if (role === 'REVIEWER') {
    loadExceptions();
  } else if (role === 'CONSUMER') {
    loadVerifiedLoans();
  }
}

// Animate numbers counting up
function animateValue(id, start, end, duration = 600, suffix = '') {
  const obj = document.getElementById(id);
  if (!obj) return;
  if (start === end) {
    obj.innerText = `${end}${suffix}`;
    return;
  }
  const range = end - start;
  let current = start;
  const increment = end > start ? 1 : -1;
  const stepTime = Math.abs(Math.floor(duration / range)) || 20;
  const timer = setInterval(() => {
    current += increment;
    obj.innerText = `${current}${suffix}`;
    if (current === end) {
      clearInterval(timer);
    }
  }, stepTime);
}

async function fetchSummary() {
  try {
    const res = await fetch(`${API_BASE}/summary`);
    if (!res.ok) return;
    const data = await res.json();
    
    const curTotal = parseInt(document.getElementById('statTotal').innerText) || 0;
    const curEx = parseInt(document.getElementById('statExceptions').innerText) || 0;
    const curVer = parseInt(document.getElementById('statVerified').innerText) || 0;

    animateValue('statTotal', curTotal, data.totalLoans || 0);
    animateValue('statExceptions', curEx, data.openExceptions || 0);
    animateValue('statVerified', curVer, data.verifiedRecords || 0);
    
    const scoreVal = data.dataQualityScore !== undefined ? Math.round(data.dataQualityScore) : 100;
    document.getElementById('statScore').innerText = `${scoreVal}%`;
  } catch (e) {
    console.error("Failed to fetch summary metrics:", e);
  }
}

async function uploadLoanTape() {
  const fileInput = document.getElementById('csvFileInput');
  if (!fileInput.files.length) {
    showToast("File Required", "Please choose a CSV tape file to ingest.", "warning");
    return;
  }

  const btn = event?.currentTarget || document.querySelector('.btn-indigo-grad');
  const originalBtnHtml = btn ? btn.innerHTML : '';
  if (btn) {
    btn.disabled = true;
    btn.innerHTML = `<span class="spinner-border spinner-border-sm me-2"></span>Ingesting...`;
  }

  const formData = new FormData();
  formData.append("file", fileInput.files[0]);
  formData.append("uploader", "operator@intain.com");

  try {
    const res = await fetch(`${API_BASE}/upload`, { method: 'POST', body: formData });
    if (!res.ok) throw new Error("Upload processing error");
    const data = await res.json();

    document.getElementById('uploadSummary').innerHTML = `
      <div class="p-3 rounded-3 bg-emerald-subtle border border-emerald-subtle d-flex align-items-center justify-content-between shadow-sm">
        <div class="d-flex align-items-center gap-3">
          <div class="p-2 rounded-circle bg-white border border-emerald-subtle">
            <i class="bi bi-check2-circle text-emerald fs-4 d-block"></i>
          </div>
          <div>
            <div class="fw-bold text-dark fs-8">INGESTION COMPLETE: ${data.filename}</div>
            <div class="text-secondary fs-8 font-mono mt-0.5">
              Parsed: <span class="fw-bold text-dark">${data.totalRecords}</span> | 
              Clean: <span class="text-emerald fw-bold">${data.validRecords}</span> | 
              Flagged Anomalies: <span class="text-rose fw-bold">${data.failedRecords}</span>
            </div>
          </div>
        </div>
        <span class="badge bg-white text-dark border font-mono fs-8 py-1-5 px-2-5 shadow-xs">200 CREATED</span>
      </div>`;

    fetchSummary();
    addHistoryRow(data);
    fileInput.value = '';
    showToast("Batch Committed", `Successfully verified ${data.filename}`, "success");
  } catch (e) {
    showToast("Upload Error", "Verify Spring Boot is active on port 8085.", "danger");
  } finally {
    if (btn) {
      btn.disabled = false;
      btn.innerHTML = originalBtnHtml;
    }
  }
}

function addHistoryRow(data) {
  const tbody = document.getElementById('historyTableBody');
  if (tbody.innerHTML.includes("No batch files ingested")) {
    tbody.innerHTML = '';
  }
  tbody.innerHTML = `
    <tr>
      <td><span class="badge bg-blue-subtle font-mono">#RUN-${data.id}</span></td>
      <td><b class="text-dark">${data.filename}</b></td>
      <td class="font-mono fs-8 text-secondary">${data.uploadedBy}</td>
      <td class="fw-bold text-dark">${data.totalRecords}</td>
      <td><span class="badge bg-emerald-subtle font-mono">${data.validRecords}</span></td>
      <td><span class="badge bg-rose-subtle font-mono">${data.failedRecords}</span></td>
      <td class="font-mono fs-8 text-muted">${new Date(data.uploadTimestamp).toLocaleTimeString()}</td>
    </tr>` + tbody.innerHTML;
}

async function loadExceptions() {
  const severity = document.getElementById('severityFilter').value;
  const url = severity ? `${API_BASE}/exceptions?severity=${severity}` : `${API_BASE}/exceptions`;
  
  try {
    const res = await fetch(url);
    const data = await res.json();
    const tbody = document.getElementById('exceptionsTableBody');
    tbody.innerHTML = '';

    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-5"><i class="bi bi-shield-check text-emerald fs-3 d-block mb-2"></i>No unresolved exceptions in queue.</td></tr>';
      return;
    }

    data.forEach(ex => {
      const badgeClass = ex.severity === 'CRITICAL' ? 'bg-rose-subtle' :
                         ex.severity === 'HIGH' ? 'bg-amber-subtle' :
                         ex.severity === 'MEDIUM' ? 'bg-blue-subtle' : 'bg-light text-secondary border';

      tbody.innerHTML += `
        <tr>
          <td><span class="font-mono fs-8 text-muted">#EX-${ex.id}</span></td>
          <td><span class="badge bg-light border text-dark font-mono">${ex.loanRecord?.loanId || 'N/A'}</span></td>
          <td><code class="font-mono fw-bold" style="color: #4f46e5;">${ex.exceptionCode}</code></td>
          <td class="font-mono text-secondary fs-8">${ex.fieldName}</td>
          <td><span class="badge ${badgeClass} font-mono">${ex.severity}</span></td>
          <td class="text-secondary fs-8">${ex.description}</td>
          <td class="text-end">
            <button class="btn btn-sm btn-indigo-grad fw-bold py-1 px-3 fs-8" onclick="openCopilot(${ex.id}, ${ex.loanRecord?.id}, '${ex.fieldName}')">
              <i class="bi bi-stars me-1"></i> Remediate
            </button>
          </td>
        </tr>`;
    });
  } catch (e) {
    console.error("Failed to load exceptions:", e);
  }
}

async function openCopilot(exceptionId, loanId, fieldName) {
  selectedExceptionId = exceptionId;
  selectedLoanId = loanId;
  copilotModalInstance.show();

  document.getElementById('aiLoading').classList.remove('d-none');
  document.getElementById('aiContent').classList.add('d-none');
  document.getElementById('flaggedFieldInput').value = fieldName;
  document.getElementById('resolutionNoteInput').value = 'Audited & validated against contract manifest.';

  try {
    const res = await fetch(`${API_BASE}/exceptions/${exceptionId}/ai-assist`, { method: 'POST' });
    const data = await res.json();
    document.getElementById('aiExplanationText').innerText = data.recommendation || 'Rule violation analyzed.';
  } catch (e) {
    document.getElementById('aiExplanationText').innerText = 'Field discrepancy flagged. Suggested review of contract terms and adjustment of current balances.';
  } finally {
    document.getElementById('aiLoading').classList.add('d-none');
    document.getElementById('aiContent').classList.remove('d-none');
  }
}

async function confirmResolution() {
  if (!selectedLoanId) return;
  try {
    const res = await fetch(`${API_BASE}/loans/${selectedLoanId}/verify?verifiedBy=auditor@intain.com`, { method: 'POST' });
    if (res.ok) {
      copilotModalInstance.hide();
      showToast("Attested & Committed", `Loan record #${selectedLoanId} verified and hashed.`, "success");
      loadExceptions();
      fetchSummary();
    }
  } catch (e) {
    showToast("Attestation Failed", "Error confirming verification.", "danger");
  }
}

async function loadVerifiedLoans() {
  try {
    const res = await fetch(`${API_BASE}/verified-loans`);
    const data = await res.json();
    const tbody = document.getElementById('verifiedTableBody');
    tbody.innerHTML = '';

    if (!data.length) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-5"><i class="bi bi-database text-muted fs-3 d-block mb-2"></i>Zero records committed to immutable ledger.</td></tr>';
      return;
    }

    data.forEach(v => {
      tbody.innerHTML += `
        <tr>
          <td><span class="badge bg-purple-subtle font-mono">#VREC-${v.id}</span></td>
          <td><span class="badge bg-light border text-dark font-mono">${v.loanRecord?.loanId || 'N/A'}</span></td>
          <td class="font-mono fs-8 text-secondary">${v.verifiedBy}</td>
          <td class="font-mono fs-8 text-muted">${new Date(v.verifiedAt).toLocaleString()}</td>
          <td><span class="badge bg-emerald-subtle font-mono fs-8"><i class="bi bi-key-fill me-1"></i>${v.recordHash ? v.recordHash.substring(0, 20) + '...' : 'GEN-HASH'}</span></td>
          <td class="text-end">
            <button class="btn btn-sm btn-outline-secondary py-1 px-3 fs-8" onclick="viewAudit(${v.loanRecord?.id})">
              <i class="bi bi-shield-check me-1"></i> Inspect Lineage
            </button>
          </td>
        </tr>`;
    });
  } catch (e) {
    console.error("Failed to fetch verified records:", e);
  }
}

async function viewAudit(loanId) {
  if (!loanId) return;
  try {
    const res = await fetch(`${API_BASE}/audit/${loanId}`);
    const logs = await res.json();
    const tbody = document.getElementById('auditTableBody');
    tbody.innerHTML = '';

    logs.forEach(l => {
      tbody.innerHTML += `
        <tr>
          <td><span class="badge bg-blue-subtle font-mono">${l.actionType}</span></td>
          <td class="font-mono fs-8 text-dark fw-semibold">${l.performedBy}</td>
          <td class="font-mono fs-8 text-secondary">${l.details}</td>
          <td class="font-mono fs-8 text-muted">${new Date(l.timestamp).toLocaleTimeString()}</td>
        </tr>`;
    });

    auditModalInstance.show();
  } catch (e) {
    console.error("Failed to inspect audit log:", e);
  }
}

// Lightweight Feedback Toast Helper
function showToast(title, message, type = 'info') {
  let toastContainer = document.getElementById('appToastContainer');
  if (!toastContainer) {
    toastContainer = document.createElement('div');
    toastContainer.id = 'appToastContainer';
    toastContainer.className = 'toast-container position-fixed bottom-0 end-0 p-3';
    toastContainer.style.zIndex = '9999';
    document.body.appendChild(toastContainer);
  }

  const toastEl = document.createElement('div');
  const borderClass = type === 'success' ? 'border-emerald' : type === 'danger' ? 'border-rose' : 'border-blue';
  toastEl.className = `toast align-items-center bg-white border shadow-lg ${borderClass}`;
  toastEl.setAttribute('role', 'alert');
  toastEl.setAttribute('aria-live', 'assertive');
  toastEl.setAttribute('aria-atomic', 'true');

  toastEl.innerHTML = `
    <div class="d-flex">
      <div class="toast-body py-2 px-3">
        <div class="fw-bold text-dark fs-8">${title}</div>
        <div class="text-secondary small">${message}</div>
      </div>
      <button type="button" class="btn-close me-2 m-auto shadow-none" data-bs-dismiss="toast"></button>
    </div>`;

  toastContainer.appendChild(toastEl);
  const toast = new bootstrap.Toast(toastEl, { delay: 3500 });
  toast.show();
  toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}