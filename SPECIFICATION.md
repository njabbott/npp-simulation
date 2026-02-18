# NPP Simulation Application - Specification

## Purpose

This application was built as a hands-on learning and demonstration tool for Australia's **New Payments Platform (NPP)**. It serves two goals:

1. **Interview preparation** - To demonstrate to a potential employer a deep, practical understanding of NPP architecture, not just theoretical knowledge. Rather than simply reading about PayID, Osko, PayTo, and ISO 20022, this project proves the ability to translate that understanding into working software.

2. **Learning resource** - To provide a runnable reference for anyone else wanting to understand how the NPP works. The codebase models the real payment lifecycle (initiation, clearing, settlement, confirmation), generates genuine ISO 20022 XML messages using the Prowide library, and simulates the FSS settlement of ESA balances between participant banks. Reading through the code and interacting with the UI should give a practical feel for how these systems fit together.

## Overview

A full-stack demo application simulating Australia's **New Payments Platform (NPP)** architecture. Covers PayID resolution, Osko-style real-time payments, PayTo mandate management, ISO 20022 messaging (pacs.008, pacs.002, pacs.004), and real-time gross settlement via the Fast Settlement Service (FSS).

---

## Tech Stack

| Component | Choice |
|-----------|--------|
| Backend | Spring Boot 3.4.x, Java 21 LTS |
| Frontend | React 18 + Vite, embedded via `frontend-maven-plugin`, plain CSS |
| Database | H2 in-memory (`jdbc:h2:mem:nppdb`) |
| ISO 20022 | Prowide ISO 20022 (`pw-iso20022:SRU2024-10.2.6`) |
| Real-time updates | Server-Sent Events (SSE) via `SseEmitter` |

---

## Domain Model

### Enums

| Enum | Values |
|------|--------|
| `PaymentStatus` | INITIATED, CLEARING, SETTLED, CONFIRMED, REJECTED, RETURNED |
| `PayIdType` | PHONE, EMAIL, ABN |
| `MandateStatus` | PENDING, ACTIVE, REJECTED, SUSPENDED, CANCELLED |
| `Iso20022MessageType` | PAIN_001, PACS_008, PACS_002, PACS_004, CAMT_056 |

### Entities

| Entity | Key Fields | Relationships |
|--------|-----------|---------------|
| `NppParticipant` | name, shortName, bic (unique), bsb, esaBalance | - |
| `BankAccount` | accountNumber, bsb, accountName, balance | `@ManyToOne NppParticipant` |
| `PayId` | type, value, displayName, active | `@ManyToOne BankAccount` |
| `NppPayment` | paymentId (UUID), endToEndId, amount, currency, status, remittanceInfo, payIdUsed, rejectionReason | `@ManyToOne BankAccount` (debtor + creditor), `@ManyToOne NppParticipant` (debtorAgent + creditorAgent) |
| `SettlementRecord` | amount, debitBalanceAfter, creditBalanceAfter, settledAt | `@ManyToOne NppPayment`, `@ManyToOne NppParticipant` (debit + credit) |
| `PayToMandate` | mandateId (UUID), description, maximumAmount, frequency, status, validFrom, validTo | `@ManyToOne BankAccount` (creditor + debtor) |
| `Iso20022Message` | messageType, messageId, xmlContent (@Lob), direction, senderBic, receiverBic | `@ManyToOne NppPayment` |

---

## Seed Data

### Participants (5 Banks)

| Bank | Short Name | BIC | BSB | ESA Balance |
|------|-----------|-----|-----|-------------|
| Commonwealth Bank of Australia | CBA | CTBAAU2S | 062-000 | $50,000,000.00 |
| National Australia Bank | NAB | NATAAU33 | 083-000 | $48,000,000.00 |
| Australia and New Zealand Banking Group | ANZ | ANZBAU3M | 012-000 | $47,000,000.00 |
| Westpac Banking Corporation | Westpac | WPACAU2S | 032-000 | $45,000,000.00 |
| People First Bank | PFB | HBSLAU4T | 638-060 | $42,000,000.00 |

### Bank Accounts (9)

| Account Name | BSB | Account Number | Bank | Balance |
|-------------|-----|---------------|------|---------|
| John Smith | 638-060 | 12345678 | PFB | $15,420.50 |
| Sarah Johnson | 062-000 | 87654321 | CBA | $8,750.00 |
| ACME Pty Ltd | 062-000 | 11112222 | CBA | $250,000.00 |
| Mike Wilson | 083-000 | 22334455 | NAB | $32,100.75 |
| TechCorp Australia | 083-000 | 55667788 | NAB | $180,000.00 |
| Emma Davis | 012-000 | 33445566 | ANZ | $5,200.30 |
| Green Energy Solutions | 012-000 | 66778899 | ANZ | $420,000.00 |
| James Brown | 032-000 | 44556677 | Westpac | $18,900.00 |
| OzTrade Imports | 032-000 | 99887766 | Westpac | $95,000.00 |

### PayIDs (8)

| Type | Value | Display Name | Linked Account |
|------|-------|-------------|----------------|
| PHONE | +61412345678 | John S | John Smith (PFB) |
| PHONE | +61498765432 | Mike W | Mike Wilson (NAB) |
| PHONE | +61423456789 | Emma D | Emma Davis (ANZ) |
| EMAIL | sarah.j@email.com | Sarah Johnson | Sarah Johnson (CBA) |
| EMAIL | james.b@email.com | James Brown | James Brown (Westpac) |
| ABN | 51824753556 | ACME Pty Ltd | ACME Pty Ltd (CBA) |
| ABN | 12345678901 | TechCorp Australia | TechCorp Australia (NAB) |
| ABN | 98765432100 | Green Energy Solutions | Green Energy Solutions (ANZ) |

### PayTo Mandates (2)

| Description | Status | Creditor | Debtor | Max Amount | Frequency |
|------------|--------|----------|--------|------------|-----------|
| Monthly electricity bill | ACTIVE | Green Energy Solutions (ANZ) | John Smith (PFB) | $500.00 | MONTHLY |
| Gym membership subscription | PENDING | TechCorp Australia (NAB) | Emma Davis (ANZ) | $120.00 | MONTHLY |

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
| GET | `/api/dashboard` | Aggregate dashboard stats |

---

## Payment Request Schema

```json
{
  "amount": 100.00,
  "payIdType": "PHONE",
  "payIdValue": "+61412345678",
  "creditorBsb": null,
  "creditorAccountNumber": null,
  "debtorBsb": "638-060",
  "debtorAccountNumber": "12345678",
  "remittanceInfo": "Invoice #1234"
}
```

Either `payIdType`/`payIdValue` or `creditorBsb`/`creditorAccountNumber` must be provided. Debtor fields are always required.

---

## Payment Lifecycle

The payment lifecycle is processed asynchronously with simulated delays:

```
INITIATED ──(500ms)──> CLEARING ──(800ms)──> SETTLED ──(200ms)──> CONFIRMED
                           │
                           └──(~10% chance)──> REJECTED
```

1. **INITIATED** - Payment created, `pacs.008` (FIToFICustomerCreditTransfer) generated
2. **CLEARING** - Payment enters NPP clearing (500ms delay)
3. **~10% random rejection** at clearing stage with realistic reasons:
   - Account closed
   - Invalid BSB
   - Account frozen - regulatory hold
   - Beneficiary name mismatch
   - Transaction limit exceeded
4. **SETTLED** - FSS settles ESA balances between banks (800ms delay). Can also reject here if ESA balance insufficient.
5. **CONFIRMED** - `pacs.002` (FIToFIPaymentStatusReport) with ACCP status generated (200ms delay)

On rejection, a `pacs.002` with RJCT status is generated. On return, a `pacs.004` (PaymentReturn) is generated and settlement is reversed.

Real-time status updates are delivered to the frontend via **Server-Sent Events (SSE)** with a 60-second timeout.

---

## ISO 20022 Messages

### pacs.008.001.08 - FIToFICustomerCreditTransfer

Generated when a payment is initiated. Contains:
- Group header with message ID, settlement method (CLRG), interbank settlement amount/date
- Credit transfer transaction with payment IDs, amount in AUD, debtor/creditor agents (by BIC), debtor/creditor names, remittance information

### pacs.002.001.10 - FIToFIPaymentStatusReport

Generated on payment confirmation (ACCP) or rejection (RJCT). Contains:
- Group header with message ID
- Transaction status with original end-to-end ID, status code, and rejection reason if applicable

### pacs.004.001.09 - PaymentReturn

Generated when a confirmed payment is returned. Contains:
- Group header with settlement method (CLRG), returned amount
- Original transaction reference with return reason

All messages are built using the **Prowide ISO 20022** library (`MxPacs00800108`, `MxPacs00200110`, `MxPacs00400109`) with fallback XML generation if the library encounters issues.

---

## FSS (Fast Settlement Service) Simulation

Settlement operates on Exchange Settlement Account (ESA) balances held by each participant bank at the Reserve Bank of Australia:

1. Validate the debtor bank's ESA balance is sufficient
2. Debit the debtor bank's ESA by the payment amount
3. Credit the creditor bank's ESA by the payment amount
4. Create a `SettlementRecord` with post-settlement balances

Settlement reversal follows the inverse process for returned payments.

---

## Frontend Pages

| Page | Route | Key Features |
|------|-------|-------------|
| Dashboard | `/dashboard` | Stat cards (total/settled/rejected), bank ESA balances, recent payments table. Auto-refreshes every 5s. |
| PayID Lookup | `/payid` | Dropdown (PHONE/EMAIL/ABN) + input. Resolves to account details. Quick-select buttons for seeded PayIDs. |
| Send Payment | `/send` | Toggle PayID vs BSB/account mode. Pre-registered PayID dropdown with auto-resolve on selection. "Add a new PayID" link toggles manual entry (mutually exclusive with dropdown). SSE subscription shows real-time status progression bar. Clickable ISO 20022 message rows expand to show XML content with syntax highlighting. |
| PayTo Mandates | `/payto` | Mandate table with status badges. Create form. Approve/reject buttons on PENDING mandates. Execute payment button on ACTIVE mandates with amount validation. |
| Settlement Monitor | `/settlement` | 4 bank ESA balance cards. Settlement transaction log table. Auto-refreshes every 3s. |
| Message Inspector | `/messages` | Message list with type badges (pacs.008/002/004). Click row to expand and view XML with syntax highlighting. |
| About | `/about` | Research project overview, creator credit (Nick Abbott), architecture/tech stack breakdown, link to Swagger API docs, link to GitHub source. Linked from the bottom-left sidebar in place of the former "Australia NPP Simulation" label. |

### UX Features

- **Form state persistence**: PayID Lookup, Send Payment, and PayTo Mandates pages persist their form state (text inputs, dropdown selections, results) when navigating between pages. State is lifted into `App.jsx` and passed as props so it survives component unmount/remount cycles.
- **Placeholder styling**: Form input placeholder text uses `rgb(204, 204, 204)` to clearly distinguish it from user-entered values.
- **About link in sidebar footer**: The sidebar footer contains a NavLink to `/about` (replacing the former static "Australia NPP Simulation" label), styled consistently with the main navigation items.

---

## Configuration

### application.yml

| Property | Value | Description |
|----------|-------|-------------|
| `spring.datasource.url` | `jdbc:h2:mem:nppdb` | In-memory H2 database |
| `spring.jpa.hibernate.ddl-auto` | `create-drop` | Schema recreated on each restart |
| `spring.h2.console.enabled` | `true` | H2 console at `/h2-console` |
| `spring.h2.console.path` | `/h2-console` | H2 console path |
| `npp.simulation.clearing-delay-ms` | `500` | Simulated clearing delay |
| `npp.simulation.settlement-delay-ms` | `800` | Simulated settlement delay |
| `npp.simulation.confirmation-delay-ms` | `200` | Simulated confirmation delay |

### H2 Console Access

- **URL**: `http://localhost:8080/h2-console`
- **JDBC URL**: `jdbc:h2:mem:nppdb`
- **Username**: `sa`
- **Password**: *(blank)*

---

## Running the Application

```bash
# Development - backend
mvn spring-boot:run

# Development - frontend (with hot reload)
cd frontend && npm run dev
# Frontend dev server at http://localhost:5173 (proxies /api to :8080)

# Production build
mvn clean package
java -jar target/npp-demo-1.0-SNAPSHOT.jar
# Full application at http://localhost:8080
```

---

## Project Structure

```
Payment Processing/
├── pom.xml
├── frontend/
│   ├── package.json
│   ├── vite.config.js
│   ├── index.html
│   └── src/
│       ├── main.jsx
│       ├── App.jsx
│       ├── api.js
│       ├── index.css
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
└── src/
    ├── main/
    │   ├── java/com/nick/npp/
    │   │   ├── NppDemoApplication.java
    │   │   ├── config/
    │   │   │   ├── AsyncConfig.java
    │   │   │   ├── WebConfig.java
    │   │   │   └── DataInitializer.java
    │   │   ├── controller/
    │   │   │   ├── PayIdController.java
    │   │   │   ├── PaymentController.java
    │   │   │   ├── PayToController.java
    │   │   │   ├── SettlementController.java
    │   │   │   ├── MessageController.java
    │   │   │   ├── ParticipantController.java
    │   │   │   ├── DashboardController.java
    │   │   │   └── SpaForwardController.java
    │   │   ├── dto/
    │   │   │   ├── PayIdResolutionResponse.java
    │   │   │   ├── PaymentRequest.java
    │   │   │   ├── PaymentResponse.java
    │   │   │   ├── MandateRequest.java
    │   │   │   ├── MandateResponse.java
    │   │   │   ├── MandateExecuteRequest.java
    │   │   │   ├── SettlementBalanceResponse.java
    │   │   │   ├── Iso20022MessageResponse.java
    │   │   │   └── DashboardResponse.java
    │   │   ├── exception/
    │   │   │   ├── PayIdNotFoundException.java
    │   │   │   ├── AccountNotFoundException.java
    │   │   │   ├── PaymentNotFoundException.java
    │   │   │   ├── MandateNotFoundException.java
    │   │   │   ├── InsufficientEsaBalanceException.java
    │   │   │   └── GlobalExceptionHandler.java
    │   │   ├── model/
    │   │   │   ├── PaymentStatus.java
    │   │   │   ├── PayIdType.java
    │   │   │   ├── MandateStatus.java
    │   │   │   ├── Iso20022MessageType.java
    │   │   │   ├── NppParticipant.java
    │   │   │   ├── BankAccount.java
    │   │   │   ├── PayId.java
    │   │   │   ├── NppPayment.java
    │   │   │   ├── SettlementRecord.java
    │   │   │   ├── PayToMandate.java
    │   │   │   └── Iso20022Message.java
    │   │   ├── repository/
    │   │   │   ├── NppParticipantRepository.java
    │   │   │   ├── BankAccountRepository.java
    │   │   │   ├── PayIdRepository.java
    │   │   │   ├── NppPaymentRepository.java
    │   │   │   ├── SettlementRecordRepository.java
    │   │   │   ├── PayToMandateRepository.java
    │   │   │   └── Iso20022MessageRepository.java
    │   │   └── service/
    │   │       ├── Iso20022MessageService.java
    │   │       ├── PayIdService.java
    │   │       ├── SettlementService.java
    │   │       ├── PaymentService.java
    │   │       └── PayToService.java
    │   └── resources/
    │       ├── application.yml
    │       └── static/          (built from frontend/)
    └── test/java/com/nick/npp/
        ├── NppDemoApplicationTests.java
        └── service/
            ├── PayIdServiceTest.java
            ├── PaymentServiceTest.java
            └── SettlementServiceTest.java
```
