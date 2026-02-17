import React, { useState, useEffect } from 'react';
import { getMessages } from '../api';
import { Light as SyntaxHighlighter } from 'react-syntax-highlighter';
import xml from 'react-syntax-highlighter/dist/esm/languages/hljs/xml';
import { vs2015 } from 'react-syntax-highlighter/dist/esm/styles/hljs';
import LoadingSpinner from '../components/LoadingSpinner';

SyntaxHighlighter.registerLanguage('xml', xml);

export default function MessageInspector() {
  const [messages, setMessages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [expandedId, setExpandedId] = useState(null);

  useEffect(() => {
    getMessages()
      .then(setMessages)
      .finally(() => setLoading(false));
  }, []);

  const refresh = () => {
    setLoading(true);
    getMessages()
      .then(setMessages)
      .finally(() => setLoading(false));
  };

  if (loading) return <LoadingSpinner />;

  return (
    <div>
      <div className="page-header">
        <h1>ISO 20022 Message Inspector</h1>
        <p>View generated ISO 20022 XML messages (pacs.008, pacs.002, pacs.004)</p>
      </div>

      <div style={{ marginBottom: 16 }}>
        <button className="btn btn-outline" onClick={refresh}>Refresh</button>
      </div>

      {messages.length === 0 ? (
        <div className="card">
          <p style={{ color: 'var(--gray-500)', fontSize: 14 }}>No messages yet. Send a payment to generate ISO 20022 messages.</p>
        </div>
      ) : (
        <div className="card">
          <div className="table-container">
            <table>
              <thead>
                <tr>
                  <th>Type</th>
                  <th>Message ID</th>
                  <th>Direction</th>
                  <th>Sender</th>
                  <th>Receiver</th>
                  <th>Payment</th>
                  <th>Time</th>
                </tr>
              </thead>
              <tbody>
                {messages.map((m) => (
                  <React.Fragment key={m.id}>
                    <tr
                      className="clickable-row"
                      onClick={() => setExpandedId(expandedId === m.id ? null : m.id)}
                    >
                      <td>
                        <span className={`badge badge-${m.messageType.toLowerCase()}`}>
                          {m.messageType.replace('_', '.')}
                        </span>
                      </td>
                      <td style={{ fontFamily: 'monospace', fontSize: 12 }}>{m.messageId}</td>
                      <td>{m.direction}</td>
                      <td>{m.senderBic}</td>
                      <td>{m.receiverBic}</td>
                      <td style={{ fontFamily: 'monospace', fontSize: 12 }}>
                        {m.paymentId ? m.paymentId.substring(0, 8) + '...' : '-'}
                      </td>
                      <td style={{ fontSize: 12 }}>
                        {new Date(m.createdAt).toLocaleTimeString()}
                      </td>
                    </tr>
                    {expandedId === m.id && (
                      <tr>
                        <td colSpan={7} style={{ padding: 0 }}>
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
        </div>
      )}
    </div>
  );
}
