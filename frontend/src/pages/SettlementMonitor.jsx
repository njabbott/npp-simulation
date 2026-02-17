import React, { useState, useEffect } from 'react';
import { getBalances, getTransactions } from '../api';
import LoadingSpinner from '../components/LoadingSpinner';

export default function SettlementMonitor() {
  const [balances, setBalances] = useState([]);
  const [transactions, setTransactions] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchData = () => {
    Promise.all([getBalances(), getTransactions()])
      .then(([b, t]) => {
        setBalances(b);
        setTransactions(t);
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 3000);
    return () => clearInterval(interval);
  }, []);

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="page-header">
        <h1>Settlement Monitor</h1>
        <p>FSS (Fast Settlement Service) - Real-time gross settlement of ESA balances</p>
      </div>

      <div className="balance-grid">
        {balances.map((b) => (
          <div key={b.bic} className="balance-card">
            <div className="bank-name">{b.shortName}</div>
            <div className="bank-bic">{b.bic}</div>
            <div className="esa-label">Exchange Settlement Account</div>
            <div className="esa-balance">
              ${Number(b.esaBalance).toLocaleString('en-AU', { minimumFractionDigits: 2 })}
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-header">Settlement Transaction Log</div>
        {transactions.length === 0 ? (
          <p style={{ color: 'var(--gray-500)', fontSize: 14 }}>No settlement transactions yet.</p>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Time</th>
                  <th>Amount</th>
                  <th>Debit Bank</th>
                  <th>Debit Balance After</th>
                  <th>Credit Bank</th>
                  <th>Credit Balance After</th>
                </tr>
              </thead>
              <tbody>
                {transactions.map((t) => (
                  <tr key={t.id}>
                    <td style={{ fontSize: 12 }}>{new Date(t.settledAt).toLocaleString()}</td>
                    <td className="amount" style={{ color: Number(t.amount) < 0 ? 'var(--danger)' : 'var(--success)' }}>
                      {Number(t.amount) < 0 ? '-' : ''}${Math.abs(Number(t.amount)).toFixed(2)}
                    </td>
                    <td>{t.debitParticipant?.shortName || 'N/A'}</td>
                    <td>${Number(t.debitBalanceAfter).toLocaleString('en-AU', { minimumFractionDigits: 2 })}</td>
                    <td>{t.creditParticipant?.shortName || 'N/A'}</td>
                    <td>${Number(t.creditBalanceAfter).toLocaleString('en-AU', { minimumFractionDigits: 2 })}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
}
