# NPP Simulation

A full-stack simulation of Australia's **New Payments Platform (NPP)** — a hands-on research project exploring the real-time payment infrastructure that underpins Osko, PayID, and PayTo.

Rather than studying the platform purely through documentation, this project translates that research into working software that models the real NPP payment lifecycle: initiation, clearing, settlement, and confirmation.

**Source code:** [github.com/njabbott/npp-simulation](https://github.com/njabbott/npp-simulation)

---

## What It Covers

- **PayID resolution** — resolve PHONE, EMAIL, and ABN identifiers to bank account details
- **Osko-style real-time payments** — full payment lifecycle with asynchronous processing and real-time status updates via SSE
- **PayTo mandate management** — create, approve, reject, and execute payment mandates
- **ISO 20022 messaging** — genuine XML generation using the Prowide library (pacs.008, pacs.002, pacs.004)
- **FSS settlement simulation** — real-time gross settlement between participant banks via simulated Exchange Settlement Account (ESA) balances

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| Backend | Spring Boot 3.4.x, Java 21 LTS |
| Frontend | React 18 + Vite, plain CSS |
| Database | H2 in-memory (`jdbc:h2:mem:nppdb`) |
| ISO 20022 | Prowide ISO 20022 (`pw-iso20022:SRU2024-10.2.6`) |
| Real-time updates | Server-Sent Events (SSE) via `SseEmitter` |
| API docs | springdoc-openapi (Swagger UI) |
| Build | Maven + frontend-maven-plugin |

---

## Payment Lifecycle

Payments are processed asynchronously with simulated timing to model real NPP behaviour:

```
INITIATED ──(500ms)──> CLEARING ──(800ms)──> SETTLED ──(200ms)──> CONFIRMED
                           │
                           └──(~10% chance)──> REJECTED
```

1. **INITIATED** — Payment created; `pacs.008` (FIToFICustomerCreditTransfer) generated
2. **CLEARING** — Payment enters NPP clearing; ~10% random rejection with realistic reasons (account closed, name mismatch, limit exceeded, etc.)
3. **SETTLED** — FSS debits/credits ESA balances between participant banks; rejects if ESA balance is insufficient
4. **CONFIRMED** — `pacs.002` (FIToFIPaymentStatusReport) with ACCP status generated

Rejected payments produce a `pacs.002` with RJCT status. Returned payments produce a `pacs.004` (PaymentReturn) and reverse the ESA settlement.

Real-time status updates are delivered to the frontend via **Server-Sent Events (SSE)**.

---

## Frontend Pages

| Page | Route | Description |
|------|-------|-------------|
| Dashboard | `/dashboard` | Summary stats, bank ESA balances, recent payments. Auto-refreshes every 5s. |
| PayID Lookup | `/payid` | Resolve PayIDs by PHONE, EMAIL, or ABN to account details. |
| Send Payment | `/send` | Send payments via PayID or BSB/account. Live status progress bar via SSE. Expandable ISO 20022 XML message viewer. |
| PayTo Mandates | `/payto` | Create, approve, reject, and execute payment mandates. |
| Settlement Monitor | `/settlement` | ESA balance cards and full settlement transaction log. Auto-refreshes every 3s. |
| Message Inspector | `/messages` | Browse all generated ISO 20022 messages; expand rows to view raw XML. |
| About | `/about` | Project overview and architecture summary. |

---

## REST API

### PayID
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/payid` | List all registered PayIDs |
| GET | `/api/payid/resolve?type={}&value={}` | Resolve a PayID to account details |

### Payments
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payments` | Initiate a new payment |
| GET | `/api/payments` | List all payments |
| GET | `/api/payments/{id}` | Get payment by ID |
| POST | `/api/payments/{id}/return` | Return a confirmed/settled payment |
| GET | `/api/payments/{paymentId}/events` | SSE stream for real-time status updates |

### PayTo Mandates
| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/payto/mandates` | Create a new mandate |
| GET | `/api/payto/mandates` | List all mandates |
| PUT | `/api/payto/mandates/{id}/approve` | Approve a pending mandate |
| PUT | `/api/payto/mandates/{id}/reject` | Reject a pending mandate |
| POST | `/api/payto/mandates/{id}/execute` | Execute a payment via an active mandate |

### Settlement
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/settlement/balances` | Get ESA balances for all participants |
| GET | `/api/settlement/transactions` | Get settlement transaction log |

### ISO 20022 Messages
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/messages` | List all ISO 20022 messages |
| GET | `/api/messages/{id}` | Get message by ID |
| GET | `/api/messages/{id}/xml` | Get raw XML content |

### Other
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/participants` | List all NPP participants |
| GET | `/api/dashboard` | Aggregate dashboard statistics |

The full REST API is also browsable via **Swagger UI** at `http://localhost:8080/swagger-ui.html`.

---

## Seed Data

The application starts with realistic seed data including 5 participant banks, 9 bank accounts, 8 PayIDs, and 2 PayTo mandates.

### Participant Banks

| Bank | BIC | ESA Balance |
|------|-----|-------------|
| Commonwealth Bank of Australia | CTBAAU2S | $50,000,000 |
| National Australia Bank | NATAAU33 | $48,000,000 |
| ANZ Banking Group | ANZBAU3M | $47,000,000 |
| Westpac Banking Corporation | WPACAU2S | $45,000,000 |
| People First Bank | HBSLAU4T | $42,000,000 |

---

## Running the Application

> **Note:** Maven is required. If using IntelliJ IDEA, the bundled Maven can be used at:
> `/Applications/IntelliJ IDEA.app/Contents/plugins/maven/lib/maven3/bin/mvn`

### Production build (single JAR)

```bash
mvn clean package
java -jar target/npp-demo-1.0-SNAPSHOT.jar
```

Application available at: `http://localhost:8080`

### Development (with frontend hot reload)

```bash
# Terminal 1 — backend
mvn spring-boot:run

# Terminal 2 — frontend dev server
cd frontend && npm run dev
```

Frontend dev server at `http://localhost:5173` (proxies `/api` calls to `:8080`).

---

## Useful URLs

| URL | Description |
|-----|-------------|
| `http://localhost:8080` | Main application |
| `http://localhost:8080/swagger-ui.html` | Swagger API documentation |
| `http://localhost:8080/h2-console` | H2 database console (JDBC URL: `jdbc:h2:mem:nppdb`, user: `sa`, password: blank) |

---

## Project Structure

```
Payment Processing/
├── pom.xml
├── frontend/                    # React + Vite frontend
│   ├── package.json
│   ├── vite.config.js
│   └── src/
│       ├── App.jsx
│       ├── api.js
│       ├── components/
│       │   ├── StatusBadge.jsx
│       │   ├── PaymentProgressBar.jsx
│       │   └── LoadingSpinner.jsx
│       └── pages/
│           ├── Dashboard.jsx
│           ├── PayIdLookup.jsx
│           ├── SendPayment.jsx
│           ├── PayToMandates.jsx
│           ├── SettlementMonitor.jsx
│           ├── MessageInspector.jsx
│           └── About.jsx
└── src/main/java/com/nick/npp/
    ├── config/                  # Async, CORS, data seeding, OpenAPI
    ├── controller/              # REST controllers
    ├── dto/                     # Request/response DTOs
    ├── exception/               # Domain exceptions + global handler
    ├── model/                   # JPA entities and enums
    ├── repository/              # Spring Data JPA repositories
    └── service/                 # Business logic (payments, PayID, PayTo, settlement, ISO 20022)
```

---

## Created By

**Nick Abbott**
