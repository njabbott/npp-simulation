const BASE = '/api';

async function fetchJson(url, options) {
  const res = await fetch(url, options);
  if (!res.ok) {
    const err = await res.json().catch(() => ({ message: res.statusText }));
    throw new Error(err.message || res.statusText);
  }
  return res.json();
}

// Dashboard
export const getDashboard = () => fetchJson(`${BASE}/dashboard`);

// PayID
export const resolvePayId = (type, value) =>
  fetchJson(`${BASE}/payid/resolve?type=${encodeURIComponent(type)}&value=${encodeURIComponent(value)}`);

// Payments
export const getPayments = () => fetchJson(`${BASE}/payments`);
export const getPayment = (id) => fetchJson(`${BASE}/payments/${id}`);
export const initiatePayment = (data) =>
  fetchJson(`${BASE}/payments`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
export const returnPayment = (id) =>
  fetchJson(`${BASE}/payments/${id}/return`, { method: 'POST' });

// SSE subscription
export const subscribeToPayment = (paymentId, onEvent) => {
  const eventSource = new EventSource(`${BASE}/payments/${paymentId}/events`);
  eventSource.addEventListener('status', (e) => {
    onEvent(JSON.parse(e.data));
  });
  eventSource.onerror = () => {
    eventSource.close();
  };
  return eventSource;
};

// PayTo Mandates
export const getMandates = () => fetchJson(`${BASE}/payto/mandates`);
export const createMandate = (data) =>
  fetchJson(`${BASE}/payto/mandates`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });
export const approveMandate = (id) =>
  fetchJson(`${BASE}/payto/mandates/${id}/approve`, { method: 'PUT' });
export const rejectMandate = (id) =>
  fetchJson(`${BASE}/payto/mandates/${id}/reject`, { method: 'PUT' });
export const executeMandate = (id, data) =>
  fetchJson(`${BASE}/payto/mandates/${id}/execute`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(data),
  });

// Settlement
export const getBalances = () => fetchJson(`${BASE}/settlement/balances`);
export const getTransactions = () => fetchJson(`${BASE}/settlement/transactions`);

// Messages
export const getMessages = () => fetchJson(`${BASE}/messages`);
export const getMessage = (id) => fetchJson(`${BASE}/messages/${id}`);

// Participants
export const getParticipants = () => fetchJson(`${BASE}/participants`);
