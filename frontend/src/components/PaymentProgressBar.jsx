import React from 'react';

const STAGES = ['INITIATED', 'CLEARING', 'SETTLED', 'CONFIRMED'];

function stageIndex(status) {
  if (status === 'REJECTED' || status === 'RETURNED') return -1;
  return STAGES.indexOf(status);
}

export default function PaymentProgressBar({ status }) {
  const current = stageIndex(status);
  const isRejected = status === 'REJECTED';
  const isReturned = status === 'RETURNED';

  return (
    <div className="progress-bar">
      {STAGES.map((stage, i) => (
        <React.Fragment key={stage}>
          {i > 0 && (
            <div className={`progress-line${current >= i ? ' filled' : ''}`} />
          )}
          <div
            className={`progress-step${
              i === current ? ' active' : ''
            }${i < current ? ' completed' : ''}${
              isRejected && i === 1 ? ' rejected' : ''
            }`}
          >
            <div className="progress-dot">
              {i < current ? '\u2713' : isRejected && i === 1 ? '\u2717' : i + 1}
            </div>
            <div className="progress-label">{stage}</div>
          </div>
        </React.Fragment>
      ))}
      {(isRejected || isReturned) && (
        <>
          <div className="progress-line" />
          <div className={`progress-step ${isRejected ? 'rejected' : 'active'}`}>
            <div className="progress-dot">{isRejected ? '\u2717' : '\u21A9'}</div>
            <div className="progress-label">{status}</div>
          </div>
        </>
      )}
    </div>
  );
}
