import React, { useState, useEffect } from 'react';
import { getDashboard } from '../api';
import StatusBadge from '../components/StatusBadge';
import LoadingSpinner from '../components/LoadingSpinner';

export default function Dashboard() {
  const [data, setData] = useState(null);
  const [error, setError] = useState(null);

  const fetchData = () => {
    getDashboard()
      .then(setData)
      .catch((e) => setError(e.message));
  };

  useEffect(() => {
    fetchData();
    const interval = setInterval(fetchData, 5000);
    return () => clearInterval(interval);
  }, []);

  if (error) return <div className="error-box">{error}</div>;
  if (!data) return <LoadingSpinner />;

  return (
    <div>
      <div className="page-header">
        <h1>NPP Dashboard</h1>
        <p>Real-time overview of the New Payments Platform simulation</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card info">
          <div className="stat-label">Total Payments</div>
          <div className="stat-value">{data.totalPayments}</div>
        </div>
        <div className="stat-card success">
          <div className="stat-label">Confirmed</div>
          <div className="stat-value">{data.confirmedPayments}</div>
        </div>
        <div className="stat-card warning">
          <div className="stat-label">Settled</div>
          <div className="stat-value">{data.settledPayments}</div>
        </div>
        <div className="stat-card danger">
          <div className="stat-label">Rejected</div>
          <div className="stat-value">{data.rejectedPayments}</div>
        </div>
      </div>

      <div className="balance-grid">
        {[...data.balances].sort((a, b) => a.bic === 'HBSLAU4T' ? -1 : b.bic === 'HBSLAU4T' ? 1 : 0).map((b) => (
          <div key={b.bic} className="balance-card">
            <div className="bank-name">{b.participantName}</div>
            <div className="bank-bic">{b.bic}</div>
            <div className="esa-label">ESA Balance</div>
            <div className="esa-balance">
              ${Number(b.esaBalance).toLocaleString('en-AU', { minimumFractionDigits: 2 })}
            </div>
          </div>
        ))}
      </div>

      <div className="card">
        <div className="card-header">Recent Payments</div>
        {data.recentPayments.length === 0 ? (
          <p style={{ color: 'var(--gray-500)', fontSize: 14 }}>No payments yet. Send one from the Send Payment page.</p>
        ) : (
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Payment ID</th>
                  <th>Amount</th>
                  <th>From</th>
                  <th>To</th>
                  <th>Status</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {data.recentPayments.map((p) => (
                  <tr key={p.paymentId}>
                    <td style={{ fontFamily: 'monospace', fontSize: 12 }}>
                      {p.paymentId.substring(0, 8)}...
                    </td>
                    <td className="amount">${Number(p.amount).toFixed(2)}</td>
                    <td>{p.debtorAccountName}</td>
                    <td>{p.creditorAccountName}</td>
                    <td><StatusBadge status={p.status} /></td>
                    <td style={{ fontSize: 12 }}>
                      {new Date(p.createdAt).toLocaleTimeString()}
                    </td>
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
