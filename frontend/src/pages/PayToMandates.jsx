import React, { useState, useEffect } from 'react';
import { getMandates, createMandate, approveMandate, rejectMandate, executeMandate } from '../api';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';

export default function PayToMandates() {
  const [mandates, setMandates] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showCreate, setShowCreate] = useState(false);
  const [showExecute, setShowExecute] = useState(null);
  const [error, setError] = useState(null);
  const [form, setForm] = useState({
    creditorBsb: '083-000',
    creditorAccountNumber: '55667788',
    debtorBsb: '062-000',
    debtorAccountNumber: '12345678',
    description: '',
    maximumAmount: '',
    frequency: 'MONTHLY',
  });
  const [executeForm, setExecuteForm] = useState({ amount: '', remittanceInfo: '' });

  const fetchMandates = () => {
    getMandates()
      .then(setMandates)
      .catch((e) => setError(e.message))
      .finally(() => setLoading(false));
  };

  useEffect(() => { fetchMandates(); }, []);

  const handleCreate = async (e) => {
    e.preventDefault();
    try {
      await createMandate({
        ...form,
        maximumAmount: parseFloat(form.maximumAmount),
      });
      setShowCreate(false);
      setForm({ ...form, description: '', maximumAmount: '' });
      fetchMandates();
    } catch (e) {
      setError(e.message);
    }
  };

  const handleApprove = async (id) => {
    try {
      await approveMandate(id);
      fetchMandates();
    } catch (e) { setError(e.message); }
  };

  const handleReject = async (id) => {
    try {
      await rejectMandate(id);
      fetchMandates();
    } catch (e) { setError(e.message); }
  };

  const handleExecute = async (id) => {
    try {
      await executeMandate(id, {
        amount: parseFloat(executeForm.amount),
        remittanceInfo: executeForm.remittanceInfo || undefined,
      });
      setShowExecute(null);
      setExecuteForm({ amount: '', remittanceInfo: '' });
    } catch (e) { setError(e.message); }
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="page-header">
        <h1>PayTo Mandates</h1>
        <p>Manage third-party payment mandates (direct debit agreements)</p>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div style={{ marginBottom: 16 }}>
        <button className="btn btn-primary" onClick={() => setShowCreate(!showCreate)}>
          {showCreate ? 'Cancel' : 'Create Mandate'}
        </button>
      </div>

      {showCreate && (
        <div className="card">
          <div className="card-header">New PayTo Mandate</div>
          <form onSubmit={handleCreate}>
            <div className="form-row">
              <div className="form-group">
                <label>Creditor BSB</label>
                <input value={form.creditorBsb} onChange={(e) => setForm({ ...form, creditorBsb: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Creditor Account</label>
                <input value={form.creditorAccountNumber} onChange={(e) => setForm({ ...form, creditorAccountNumber: e.target.value })} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Debtor BSB</label>
                <input value={form.debtorBsb} onChange={(e) => setForm({ ...form, debtorBsb: e.target.value })} required />
              </div>
              <div className="form-group">
                <label>Debtor Account</label>
                <input value={form.debtorAccountNumber} onChange={(e) => setForm({ ...form, debtorAccountNumber: e.target.value })} required />
              </div>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Description</label>
                <input value={form.description} onChange={(e) => setForm({ ...form, description: e.target.value })} placeholder="Monthly subscription" required />
              </div>
              <div className="form-group">
                <label>Maximum Amount (AUD)</label>
                <input type="number" step="0.01" min="0.01" value={form.maximumAmount} onChange={(e) => setForm({ ...form, maximumAmount: e.target.value })} required />
              </div>
            </div>
            <div className="form-group">
              <label>Frequency</label>
              <select value={form.frequency} onChange={(e) => setForm({ ...form, frequency: e.target.value })}>
                <option value="WEEKLY">Weekly</option>
                <option value="FORTNIGHTLY">Fortnightly</option>
                <option value="MONTHLY">Monthly</option>
                <option value="QUARTERLY">Quarterly</option>
                <option value="ANNUALLY">Annually</option>
              </select>
            </div>
            <button className="btn btn-primary" type="submit">Create</button>
          </form>
        </div>
      )}

      <div className="card">
        <div className="table-container">
          <table>
            <thead>
              <tr>
                <th>Mandate ID</th>
                <th>Description</th>
                <th>Max Amount</th>
                <th>Frequency</th>
                <th>Creditor</th>
                <th>Debtor</th>
                <th>Status</th>
                <th>Actions</th>
              </tr>
            </thead>
            <tbody>
              {mandates.map((m) => (
                <tr key={m.id}>
                  <td style={{ fontFamily: 'monospace', fontSize: 12 }}>{m.mandateId.substring(0, 8)}...</td>
                  <td>{m.description}</td>
                  <td className="amount">${Number(m.maximumAmount).toFixed(2)}</td>
                  <td>{m.frequency}</td>
                  <td>{m.creditorAccountName}</td>
                  <td>{m.debtorAccountName}</td>
                  <td><StatusBadge status={m.status} /></td>
                  <td>
                    {m.status === 'PENDING' && (
                      <>
                        <button className="btn btn-success btn-sm" onClick={() => handleApprove(m.id)} style={{ marginRight: 4 }}>Approve</button>
                        <button className="btn btn-danger btn-sm" onClick={() => handleReject(m.id)}>Reject</button>
                      </>
                    )}
                    {m.status === 'ACTIVE' && (
                      <button className="btn btn-primary btn-sm" onClick={() => setShowExecute(showExecute === m.id ? null : m.id)}>
                        Execute
                      </button>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {showExecute && (
        <div className="card">
          <div className="card-header">Execute Payment via Mandate</div>
          <div className="form-row">
            <div className="form-group">
              <label>Amount (AUD)</label>
              <input type="number" step="0.01" min="0.01" value={executeForm.amount}
                     onChange={(e) => setExecuteForm({ ...executeForm, amount: e.target.value })} />
            </div>
            <div className="form-group">
              <label>Remittance Info</label>
              <input value={executeForm.remittanceInfo}
                     onChange={(e) => setExecuteForm({ ...executeForm, remittanceInfo: e.target.value })}
                     placeholder="February electricity" />
            </div>
          </div>
          <button className="btn btn-primary" onClick={() => handleExecute(showExecute)}>Send Payment</button>
        </div>
      )}
    </div>
  );
}
