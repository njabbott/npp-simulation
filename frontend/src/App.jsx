import React, { useRef } from 'react';
import { BrowserRouter, Routes, Route, NavLink } from 'react-router-dom';
import Dashboard from './pages/Dashboard';
import PayIdLookup from './pages/PayIdLookup';
import SendPayment from './pages/SendPayment';
import PayToMandates from './pages/PayToMandates';
import SettlementMonitor from './pages/SettlementMonitor';
import MessageInspector from './pages/MessageInspector';
import About from './pages/About';

const navItems = [
  { path: '/dashboard', label: 'Dashboard', icon: '\u2302' },
  { path: '/payid', label: 'PayID Lookup', icon: '\uD83D\uDD0D' },
  { path: '/send', label: 'Send Payment', icon: '\u27A1' },
  { path: '/payto', label: 'PayTo Mandates', icon: '\uD83D\uDCCB' },
  { path: '/settlement', label: 'Settlement', icon: '\uD83C\uDFE6' },
  { path: '/messages', label: 'ISO 20022', icon: '\uD83D\uDCE8' },
];

export default function App() {
  const payIdStateRef = useRef({});
  const sendPaymentStateRef = useRef({});
  const payToStateRef = useRef({});

  return (
    <BrowserRouter basename={import.meta.env.VITE_BASENAME || '/'}>
      <div className="app-layout">
        <aside className="sidebar">
          <div className="sidebar-header">
            <h2>NPP Simulation</h2>
            <span className="sidebar-subtitle">New Payments Platform</span>
          </div>
          <nav className="sidebar-nav">
            {navItems.map((item) => (
              <NavLink
                key={item.path}
                to={item.path}
                className={({ isActive }) =>
                  'nav-link' + (isActive ? ' nav-link-active' : '')
                }
              >
                <span className="nav-icon">{item.icon}</span>
                {item.label}
              </NavLink>
            ))}
          </nav>
          <div className="sidebar-footer">
            <NavLink
              to="/about"
              className={({ isActive }) =>
                'sidebar-about-link' + (isActive ? ' sidebar-about-link-active' : '')
              }
            >
              <span className="nav-icon">ℹ</span>
              About
            </NavLink>
          </div>
        </aside>
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/dashboard" element={<Dashboard />} />
            <Route path="/payid" element={<PayIdLookup stateRef={payIdStateRef} />} />
            <Route path="/send" element={<SendPayment stateRef={sendPaymentStateRef} />} />
            <Route path="/payto" element={<PayToMandates stateRef={payToStateRef} />} />
            <Route path="/settlement" element={<SettlementMonitor />} />
            <Route path="/messages" element={<MessageInspector />} />
            <Route path="/about" element={<About />} />
          </Routes>
        </main>
      </div>
    </BrowserRouter>
  );
}
