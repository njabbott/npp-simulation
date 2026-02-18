import React from 'react';

export default function About() {
  return (
    <div>
      <div className="page-header">
        <h1>About</h1>
        <p>NPP Simulation — a research project by Nick Abbott</p>
      </div>

      <div className="card">
        <div className="card-header">About This Project</div>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', lineHeight: '1.7' }}>
          This application was built as a hands-on research project into Australia's{' '}
          <strong>New Payments Platform (NPP)</strong> — the real-time payment infrastructure that
          underpins Osko, PayID, and PayTo. Rather than studying the platform purely through
          documentation, this project translates that research into working software that models the
          real NPP payment lifecycle: initiation, clearing, settlement, and confirmation.
        </p>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', lineHeight: '1.7', marginTop: '12px' }}>
          The simulation covers PayID resolution, Osko-style real-time payments, PayTo mandate
          management, genuine ISO 20022 XML message generation (pacs.008, pacs.002, pacs.004), and
          real-time gross settlement via a simulated Fast Settlement Service (FSS) operating on
          Exchange Settlement Account (ESA) balances.
        </p>
      </div>

      <div className="card">
        <div className="card-header">Created By</div>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', lineHeight: '1.7' }}>
          <strong>Nick Abbott</strong>
        </p>
        <p style={{ fontSize: '14px', color: 'var(--gray-500)', marginTop: '4px' }}>
          Source code:{' '}
          <a
            href="https://github.com/njabbott/npp-simulation"
            target="_blank"
            rel="noopener noreferrer"
            style={{ color: 'var(--primary)' }}
          >
            github.com/njabbott/npp-simulation
          </a>
        </p>
      </div>

      <div className="card">
        <div className="card-header">Architecture</div>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', marginBottom: '16px' }}>
          The application is a full-stack Java/React monolith. The backend exposes a REST API
          consumed by the React frontend, with real-time status updates delivered over Server-Sent
          Events (SSE). All data is held in an in-memory H2 database that is seeded with realistic
          participant banks, accounts, PayIDs, and mandates on startup.
        </p>
        <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(280px, 1fr))', gap: '12px' }}>
          <div style={{ background: 'var(--gray-50)', borderRadius: '6px', padding: '14px', border: '1px solid var(--gray-200)' }}>
            <div style={{ fontWeight: '600', fontSize: '13px', color: 'var(--gray-700)', marginBottom: '8px' }}>Backend</div>
            <ul style={{ fontSize: '13px', color: 'var(--gray-600)', paddingLeft: '16px', lineHeight: '1.8' }}>
              <li>Spring Boot 3.4.x / Java 21 LTS</li>
              <li>Spring Data JPA + H2 in-memory database</li>
              <li>Spring Async for non-blocking payment processing</li>
              <li>SseEmitter for real-time status push</li>
              <li>springdoc-openapi (Swagger UI)</li>
            </ul>
          </div>
          <div style={{ background: 'var(--gray-50)', borderRadius: '6px', padding: '14px', border: '1px solid var(--gray-200)' }}>
            <div style={{ fontWeight: '600', fontSize: '13px', color: 'var(--gray-700)', marginBottom: '8px' }}>Frontend</div>
            <ul style={{ fontSize: '13px', color: 'var(--gray-600)', paddingLeft: '16px', lineHeight: '1.8' }}>
              <li>React 18 + Vite</li>
              <li>React Router v6 (client-side routing)</li>
              <li>Plain CSS (no framework)</li>
              <li>Bundled via frontend-maven-plugin</li>
              <li>EventSource API for SSE subscription</li>
            </ul>
          </div>
          <div style={{ background: 'var(--gray-50)', borderRadius: '6px', padding: '14px', border: '1px solid var(--gray-200)' }}>
            <div style={{ fontWeight: '600', fontSize: '13px', color: 'var(--gray-700)', marginBottom: '8px' }}>ISO 20022 Messaging</div>
            <ul style={{ fontSize: '13px', color: 'var(--gray-600)', paddingLeft: '16px', lineHeight: '1.8' }}>
              <li>Prowide ISO 20022 (pw-iso20022 SRU2024-10.2.6)</li>
              <li>pacs.008.001.08 — FIToFI Credit Transfer</li>
              <li>pacs.002.001.10 — Payment Status Report</li>
              <li>pacs.004.001.09 — Payment Return</li>
            </ul>
          </div>
          <div style={{ background: 'var(--gray-50)', borderRadius: '6px', padding: '14px', border: '1px solid var(--gray-200)' }}>
            <div style={{ fontWeight: '600', fontSize: '13px', color: 'var(--gray-700)', marginBottom: '8px' }}>NPP Domain Coverage</div>
            <ul style={{ fontSize: '13px', color: 'var(--gray-600)', paddingLeft: '16px', lineHeight: '1.8' }}>
              <li>PayID resolution (PHONE, EMAIL, ABN)</li>
              <li>Osko real-time payment flow</li>
              <li>PayTo mandate lifecycle</li>
              <li>FSS settlement on ESA balances</li>
              <li>5 simulated participant banks</li>
            </ul>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="card-header">API Documentation</div>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', marginBottom: '12px' }}>
          The full REST API is documented via Swagger UI. All endpoints are browsable and can be
          exercised directly from the browser.
        </p>
        <a
          href="http://localhost:8080/swagger-ui.html"
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-primary"
          style={{ display: 'inline-flex', textDecoration: 'none' }}
        >
          Open API Documentation
        </a>
      </div>

      <div className="card">
        <div className="card-header">Source Code</div>
        <p style={{ fontSize: '14px', color: 'var(--gray-700)', marginBottom: '12px' }}>
          The full source code is publicly available on GitHub.
        </p>
        <a
          href="https://github.com/njabbott/npp-simulation"
          target="_blank"
          rel="noopener noreferrer"
          className="btn btn-outline"
          style={{ display: 'inline-flex', textDecoration: 'none' }}
        >
          View on GitHub
        </a>
      </div>
    </div>
  );
}