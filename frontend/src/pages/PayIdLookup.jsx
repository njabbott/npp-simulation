import React, { useState } from 'react';
import { resolvePayId } from '../api';

const QUICK_PAYIDS = [
  { type: 'PHONE', value: '+61412345678', label: 'John (+61412...)' },
  { type: 'PHONE', value: '+61498765432', label: 'Mike (+61498...)' },
  { type: 'EMAIL', value: 'sarah.j@email.com', label: 'Sarah (email)' },
  { type: 'EMAIL', value: 'james.b@email.com', label: 'James (email)' },
  { type: 'ABN', value: '51824753556', label: 'ACME (ABN)' },
  { type: 'ABN', value: '12345678901', label: 'TechCorp (ABN)' },
  { type: 'ABN', value: '98765432100', label: 'Green Energy (ABN)' },
  { type: 'PHONE', value: '+61423456789', label: 'Emma (+61423...)' },
];

export default function PayIdLookup() {
  const [type, setType] = useState('PHONE');
  const [value, setValue] = useState('');
  const [result, setResult] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);

  const handleResolve = async (t, v) => {
    const resolveType = t || type;
    const resolveValue = v || value;
    if (!resolveValue) return;
    setLoading(true);
    setError(null);
    setResult(null);
    try {
      const res = await resolvePayId(resolveType, resolveValue);
      setResult(res);
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const handleQuickSelect = (t, v) => {
    setType(t);
    setValue(v);
    handleResolve(t, v);
  };

  return (
    <div>
      <div className="page-header">
        <h1>PayID Lookup</h1>
        <p>Resolve a PayID to its linked bank account details</p>
      </div>

      <div className="card">
        <div className="card-header">Quick Select</div>
        <div className="quick-select">
          {QUICK_PAYIDS.map((p) => (
            <button key={p.value} onClick={() => handleQuickSelect(p.type, p.value)}>
              {p.label}
            </button>
          ))}
        </div>
      </div>

      <div className="card">
        <div className="card-header">Resolve PayID</div>
        <div className="form-row">
          <div className="form-group">
            <label>PayID Type</label>
            <select value={type} onChange={(e) => setType(e.target.value)}>
              <option value="PHONE">Phone</option>
              <option value="EMAIL">Email</option>
              <option value="ABN">ABN</option>
            </select>
          </div>
          <div className="form-group">
            <label>PayID Value</label>
            <input
              type="text"
              value={value}
              onChange={(e) => setValue(e.target.value)}
              placeholder={type === 'PHONE' ? '+61412345678' : type === 'EMAIL' ? 'user@email.com' : '12345678901'}
              onKeyDown={(e) => e.key === 'Enter' && handleResolve()}
            />
          </div>
        </div>
        <button className="btn btn-primary" onClick={() => handleResolve()} disabled={loading || !value}>
          {loading ? 'Resolving...' : 'Resolve PayID'}
        </button>
      </div>

      {error && <div className="error-box">{error}</div>}

      {result && (
        <div className="resolve-result">
          <h3>PayID Resolved Successfully</h3>
          <div className="detail-row">
            <span className="detail-label">Display Name</span>
            <span className="detail-value">{result.displayName}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">BSB</span>
            <span className="detail-value">{result.bsb}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Account Number</span>
            <span className="detail-value">{result.accountNumber}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">Bank</span>
            <span className="detail-value">{result.bankName}</span>
          </div>
          <div className="detail-row">
            <span className="detail-label">BIC</span>
            <span className="detail-value">{result.bankBic}</span>
          </div>
        </div>
      )}
    </div>
  );
}
