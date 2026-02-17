import React from 'react';

export default function LoadingSpinner({ message = 'Loading...' }) {
  return (
    <div className="loading-center">
      <div className="spinner" />
      <span>{message}</span>
    </div>
  );
}
