import React, { useState, useEffect, useRef } from 'react';
import { initiatePayment, resolvePayId, subscribeToPayment, getMessages } from '../api';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import xml from 'react-syntax-highlighter/dist/esm/languages/hljs/xml';
import { vs2015 } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import PaymentProgressBar from '../components/PaymentProgressBar';

SyntaxHighlighter.registerLanguage('xml', xml);

export default function SendPayment({ stateRef }) {
  const [mode, setMode] = useState(stateRef.current.mode ?? 'payid');
  const [form, setForm] = useState(stateRef.current.form ?? {
    amount: '',
    payIdType: 'PHONE',
    payIdValue: '',
    creditorBsb: '',
    creditorAccountNumber: '',
    debtorBsb: '638-060',
    debtorAccountNumber: '12345678',
    remittanceInfo: '',
  });
  const [resolvedPayee, setResolvedPayee] = useState(stateRef.current.resolvedPayee ?? null);
  const [paymentResult, setPaymentResult] = useState(stateRef.current.paymentResult ?? null);
  const [currentStatus, setCurrentStatus] = useState(stateRef.current.currentStatus ?? null);
  const [statusMessage, setStatusMessage] = useState(stateRef.current.statusMessage ?? '');
  const [relatedMessages, setRelatedMessages] = useState(stateRef.current.relatedMessages ?? []);
  const [expandedId, setExpandedId] = useState(null);
  const [error, setError] = useState(null);
  const [loading, setLoading] = useState(false);
  const eventSourceRef = useRef(null);

  useEffect(() => {
    stateRef.current = { mode, form, resolvedPayee, paymentResult, currentStatus, statusMessage, relatedMessages };
  }, [mode, form, resolvedPayee, paymentResult, currentStatus, statusMessage, relatedMessages, stateRef]);

  useEffect(() => {
    return () => {
      if (eventSourceRef.current) eventSourceRef.current.close();
    };
  }, []);

  const handleChange = (field, value) => {
    setForm((prev) => ({ ...prev, [field]: value }));
  };

  const handlePayIdResolve = async () => {
    if (!form.payIdValue) return;
    try {
      const res = await resolvePayId(form.payIdType, form.payIdValue);
      setResolvedPayee(res);
      setError(null);
    } catch (e) {
      setResolvedPayee(null);
      setError(e.message);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setPaymentResult(null);
    setCurrentStatus(null);
    setRelatedMessages([]);

    try {
      const payload = {
        amount: parseFloat(form.amount),
        debtorBsb: form.debtorBsb,
        debtorAccountNumber: form.debtorAccountNumber,
        remittanceInfo: form.remittanceInfo || undefined,
      };

      if (mode === 'payid') {
        payload.payIdType = form.payIdType;
        payload.payIdValue = form.payIdValue;
      } else {
        payload.creditorBsb = form.creditorBsb;
        payload.creditorAccountNumber = form.creditorAccountNumber;
      }

      const result = await initiatePayment(payload);
      setPaymentResult(result);
      setCurrentStatus(result.status);

      // Subscribe to SSE
      if (eventSourceRef.current) eventSourceRef.current.close();
      eventSourceRef.current = subscribeToPayment(result.paymentId, (event) => {
        setCurrentStatus(event.status);
        setStatusMessage(event.message);
        if (event.status === 'CONFIRMED' || event.status === 'REJECTED') {
          if (eventSourceRef.current) eventSourceRef.current.close();
          // Fetch related messages
          getMessages().then((msgs) => {
            setRelatedMessages(
              msgs.filter((m) => m.paymentId === result.paymentId)
            );
          });
        }
      });
    } catch (e) {
      setError(e.message);
    } finally {
      setLoading(false);
    }
  };

  const resetForm = () => {
    setPaymentResult(null);
    setCurrentStatus(null);
    setStatusMessage('');
    setRelatedMessages([]);
    setError(null);
    setResolvedPayee(null);
    setExpandedId(null);
    if (eventSourceRef.current) eventSourceRef.current.close();
  };

  return (
    <div>
      <div className="page-header">
        <h1>Send Payment</h1>
        <p>Initiate a real-time NPP payment with full lifecycle tracking</p>
      </div>

      {!paymentResult ? (
        <div className="card">
          <div className="toggle-group">
            <button className={mode === 'payid' ? 'active' : ''} onClick={() => setMode('payid')}>
              PayID
            </button>
            <button className={mode === 'bsb' ? 'active' : ''} onClick={() => setMode('bsb')}>
              BSB / Account
            </button>
          </div>

          <form onSubmit={handleSubmit}>
            <div className="card-header" style={{ marginTop: 8 }}>Sender</div>
            <div className="form-row">
              <div className="form-group">
                <label>BSB</label>
                <select value={form.debtorBsb} onChange={(e) => handleChange('debtorBsb', e.target.value)}>
                  <option value="062-000">062-000 (CBA)</option>
                  <option value="083-000">083-000 (NAB)</option>
                  <option value="012-000">012-000 (ANZ)</option>
                  <option value="032-000">032-000 (Westpac)</option>
                  <option value="638-060">638-060 (People First Bank)</option>
                </select>
              </div>
              <div className="form-group">
                <label>Account Number</label>
                <select
                  value={form.debtorAccountNumber}
                  onChange={(e) => handleChange('debtorAccountNumber', e.target.value)}
                >
                  <option value="12345678">12345678 - John Smith (PFB)</option>
                  <option value="87654321">87654321 - Sarah Johnson (CBA)</option>
                  <option value="11112222">11112222 - ACME Pty Ltd (CBA)</option>
                  <option value="22334455">22334455 - Mike Wilson (NAB)</option>
                  <option value="55667788">55667788 - TechCorp (NAB)</option>
                  <option value="33445566">33445566 - Emma Davis (ANZ)</option>
                  <option value="66778899">66778899 - Green Energy (ANZ)</option>
                  <option value="44556677">44556677 - James Brown (Westpac)</option>
                  <option value="99887766">99887766 - OzTrade (Westpac)</option>
                </select>
              </div>
            </div>

            <div className="card-header">Recipient</div>
            {mode === 'payid' ? (
              <>
                <div className="form-row">
                  <div className="form-group">
                    <label>PayID Type</label>
                    <select value={form.payIdType} onChange={(e) => handleChange('payIdType', e.target.value)}>
                      <option value="PHONE">Phone</option>
                      <option value="EMAIL">Email</option>
                      <option value="ABN">ABN</option>
                    </select>
                  </div>
                  <div className="form-group">
                    <label>PayID Value</label>
                    <input
                      type="text"
                      value={form.payIdValue}
                      onChange={(e) => handleChange('payIdValue', e.target.value)}
                      onBlur={handlePayIdResolve}
                      placeholder="+61412345678"
                    />
                  </div>
                </div>
                {resolvedPayee && (
                  <div className="info-box">
                    Recipient: <strong>{resolvedPayee.displayName}</strong> at {resolvedPayee.bankName} ({resolvedPayee.bsb} / {resolvedPayee.accountNumber})
                  </div>
                )}
              </>
            ) : (
              <div className="form-row">
                <div className="form-group">
                  <label>BSB</label>
                  <input
                    type="text"
                    value={form.creditorBsb}
                    onChange={(e) => handleChange('creditorBsb', e.target.value)}
                    placeholder="083-000"
                  />
                </div>
                <div className="form-group">
                  <label>Account Number</label>
                  <input
                    type="text"
                    value={form.creditorAccountNumber}
                    onChange={(e) => handleChange('creditorAccountNumber', e.target.value)}
                    placeholder="22334455"
                  />
                </div>
              </div>
            )}

            <div className="card-header">Payment Details</div>
            <div className="form-row">
              <div className="form-group">
                <label>Amount (AUD)</label>
                <input
                  type="number"
                  step="0.01"
                  min="0.01"
                  value={form.amount}
                  onChange={(e) => handleChange('amount', e.target.value)}
                  placeholder="100.00"
                  required
                />
              </div>
              <div className="form-group">
                <label>Remittance Info</label>
                <input
                  type="text"
                  value={form.remittanceInfo}
                  onChange={(e) => handleChange('remittanceInfo', e.target.value)}
                  placeholder="Invoice #1234"
                />
              </div>
            </div>

            {error && <div className="error-box">{error}</div>}

            <button className="btn btn-primary" type="submit" disabled={loading}>
              {loading ? 'Sending...' : 'Send Payment'}
            </button>
          </form>
        </div>
      ) : (
        <div>
          <div className="card">
            <div className="card-header">Payment Lifecycle</div>
            <PaymentProgressBar status={currentStatus} />
            {statusMessage && (
              <div className={`info-box${currentStatus === 'REJECTED' ? ' error-box' : ''}`}
                   style={currentStatus === 'REJECTED' ? { background: 'var(--danger-bg)', borderColor: '#fca5a5', color: 'var(--danger)' } : {}}>
                {statusMessage}
              </div>
            )}
          </div>

          <div className="card">
            <div className="card-header">Payment Details</div>
            <div className="form-row">
              <div>
                <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                  <span style={{ color: 'var(--gray-500)' }}>Payment ID</span>
                  <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{paymentResult.paymentId}</span>
                </div>
                <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                  <span style={{ color: 'var(--gray-500)' }}>End-to-End ID</span>
                  <span style={{ fontFamily: 'monospace', fontSize: 12 }}>{paymentResult.endToEndId}</span>
                </div>
                <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                  <span style={{ color: 'var(--gray-500)' }}>Amount</span>
                  <span className="amount">${Number(paymentResult.amount).toFixed(2)} AUD</span>
                </div>
              </div>
              <div>
                <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                  <span style={{ color: 'var(--gray-500)' }}>From</span>
                  <span>{paymentResult.debtorAccountName} ({paymentResult.debtorBankName})</span>
                </div>
                <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                  <span style={{ color: 'var(--gray-500)' }}>To</span>
                  <span>{paymentResult.creditorAccountName} ({paymentResult.creditorBankName})</span>
                </div>
                {paymentResult.payIdUsed && (
                  <div className="detail-row" style={{ display: 'flex', justifyContent: 'space-between', padding: '6px 0', fontSize: 14 }}>
                    <span style={{ color: 'var(--gray-500)' }}>PayID</span>
                    <span>{paymentResult.payIdUsed}</span>
                  </div>
                )}
              </div>
            </div>
          </div>

          {relatedMessages.length > 0 && (
            <div className="card">
              <div className="card-header">ISO 20022 Messages Generated</div>
              <table>
                <thead>
                  <tr>
                    <th>Type</th>
                    <th>Message ID</th>
                    <th>Direction</th>
                    <th>Sender</th>
                    <th>Receiver</th>
                  </tr>
                </thead>
                <tbody>
                  {relatedMessages.map((m) => (
                    <React.Fragment key={m.id}>
                      <tr
                        className="clickable-row"
                        onClick={() => setExpandedId(expandedId === m.id ? null : m.id)}
                      >
                        <td><span className={`badge badge-${m.messageType.toLowerCase()}`}>{m.messageType.replace('_', '.')}</span></td>
                        <td style={{ fontFamily: 'monospace', fontSize: 12 }}>{m.messageId}</td>
                        <td>{m.direction}</td>
                        <td>{m.senderBic}</td>
                        <td>{m.receiverBic}</td>
                      </tr>
                      {expandedId === m.id && (
                        <tr>
                          <td colSpan={5} style={{ padding: 0 }}>
                            <div className="xml-viewer">
                              <SyntaxHighlighter
                                language="xml"
                                style={vs2015}
                                customStyle={{ margin: 0, padding: 16 }}
                                wrapLongLines
                              >
                                {m.xmlContent}
                              </SyntaxHighlighter>
                            </div>
                          </td>
                        </tr>
                      )}
                    </React.Fragment>
                  ))}
                </tbody>
              </table>
            </div>
          )}

          <button className="btn btn-outline" onClick={resetForm}>Send Another Payment</button>
        </div>
      )}
    </div>
  );
}
