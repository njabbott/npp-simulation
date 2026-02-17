import React, { useState, useEffect, useRef } from 'react';
import { initiatePayment, resolvePayId, subscribeToPayment, getMessages, getAllPayIds } from '../api';
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
  const [registeredPayIds, setRegisteredPayIds] = useState(stateRef.current.registeredPayIds ?? []);
  const [selectedPayId, setSelectedPayId] = useState(stateRef.current.selectedPayId ?? '');
  const [showNewPayId, setShowNewPayId] = useState(stateRef.current.showNewPayId ?? false);
  const [newPayIdType, setNewPayIdType] = useState(stateRef.current.newPayIdType ?? 'PHONE');
  const [newPayIdValue, setNewPayIdValue] = useState(stateRef.current.newPayIdValue ?? '');
  const eventSourceRef = useRef(null);

  useEffect(() => {
    stateRef.current = { mode, form, resolvedPayee, paymentResult, currentStatus, statusMessage, relatedMessages, registeredPayIds, selectedPayId, showNewPayId, newPayIdType, newPayIdValue };
  }, [mode, form, resolvedPayee, paymentResult, currentStatus, statusMessage, relatedMessages, registeredPayIds, selectedPayId, showNewPayId, newPayIdType, newPayIdValue, stateRef]);

  useEffect(() => {
    return () => {
      if (eventSourceRef.current) eventSourceRef.current.close();
    };
  }, []);

  useEffect(() => {
    if (mode === 'payid') {
      getAllPayIds().then(setRegisteredPayIds).catch(() => {});
    }
  }, [mode]);

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
                  <div className="form-group" style={{ flex: 1 }}>
                    <label>Pre-Registered PayIDs</label>
                    <select
                      value={selectedPayId}
                      onChange={(e) => {
                        const idx = e.target.value;
                        setSelectedPayId(idx);
                        setShowNewPayId(false);
                        setNewPayIdValue('');
                        if (idx !== '') {
                          const payId = registeredPayIds[idx];
                          handleChange('payIdType', payId.payIdType);
                          handleChange('payIdValue', payId.value);
                          resolvePayId(payId.payIdType, payId.value)
                            .then((res) => { setResolvedPayee(res); setError(null); })
                            .catch((err) => { setResolvedPayee(null); setError(err.message); });
                        } else {
                          handleChange('payIdType', 'PHONE');
                          handleChange('payIdValue', '');
                          setResolvedPayee(null);
                        }
                      }}
                    >
                      <option value="">Please select from the following...</option>
                      {registeredPayIds.map((p, i) => (
                        <option key={i} value={i}>{p.displayName} - {p.value}</option>
                      ))}
                    </select>
                  </div>
                </div>

                {!showNewPayId && (
                  <div style={{ margin: '8px 0' }}>
                    <a
                      href="#"
                      onClick={(e) => {
                        e.preventDefault();
                        setShowNewPayId(true);
                        setSelectedPayId('');
                        setResolvedPayee(null);
                        handleChange('payIdType', 'PHONE');
                        handleChange('payIdValue', '');
                        setNewPayIdType('PHONE');
                        setNewPayIdValue('');
                      }}
                      style={{ fontSize: 14 }}
                    >
                      Add a new PayID
                    </a>
                  </div>
                )}

                {selectedPayId !== '' && resolvedPayee && (
                  <div className="info-box" style={{ marginTop: 12 }}>
                    <div style={{ marginBottom: 4 }}><strong>Existing Pay ID</strong></div>
                    <div style={{ fontSize: 14 }}>
                      <div>PayID Name: <strong>{resolvedPayee.displayName}</strong></div>
                      <div>PayID: <strong>{resolvedPayee.value}</strong></div>
                      <div>PayID Type: <strong>{resolvedPayee.payIdType}</strong></div>
                    </div>
                    <div style={{ marginTop: 8, padding: '8px 12px', background: 'var(--warning-bg, #fef3cd)', borderRadius: 6, fontSize: 13, color: 'var(--warning, #856404)' }}>
                      &#9888; Check the name and PayID entered. Entering incorrect details may result in the wrong account being credited and the funds may not be able to be recovered.
                    </div>
                  </div>
                )}

                {showNewPayId && (
                  <div style={{ marginTop: 12 }}>
                    <div className="form-row">
                      <div className="form-group">
                        <label>PayID Type</label>
                        <select
                          value={newPayIdType}
                          onChange={(e) => {
                            setNewPayIdType(e.target.value);
                            handleChange('payIdType', e.target.value);
                            setNewPayIdValue('');
                            handleChange('payIdValue', '');
                            setResolvedPayee(null);
                          }}
                        >
                          <option value="PHONE">Phone</option>
                          <option value="EMAIL">Email</option>
                          <option value="ABN">ABN</option>
                        </select>
                      </div>
                      <div className="form-group">
                        <label>{newPayIdType === 'PHONE' ? 'Phone Number' : newPayIdType === 'EMAIL' ? 'Email Address' : 'ABN'}</label>
                        <input
                          type="text"
                          value={newPayIdValue}
                          onChange={(e) => {
                            setNewPayIdValue(e.target.value);
                            handleChange('payIdValue', e.target.value);
                          }}
                          onBlur={handlePayIdResolve}
                          placeholder={newPayIdType === 'PHONE' ? '+61412345678' : newPayIdType === 'EMAIL' ? 'name@example.com' : '51824753556'}
                        />
                      </div>
                    </div>
                    {resolvedPayee && (
                      <div className="info-box">
                        Recipient: <strong>{resolvedPayee.displayName}</strong> at {resolvedPayee.bankName} ({resolvedPayee.bsb} / {resolvedPayee.accountNumber})
                      </div>
                    )}
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
