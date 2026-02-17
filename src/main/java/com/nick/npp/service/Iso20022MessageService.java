package com.nick.npp.service;

import com.nick.npp.model.*;
import com.nick.npp.repository.Iso20022MessageRepository;
import com.prowidesoftware.swift.model.mx.MxPacs00800108;
import com.prowidesoftware.swift.model.mx.MxPacs00200110;
import com.prowidesoftware.swift.model.mx.MxPacs00400109;
import com.prowidesoftware.swift.model.mx.dic.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.UUID;

@Service
public class Iso20022MessageService {

    private static final Logger log = LoggerFactory.getLogger(Iso20022MessageService.class);
    private static final ZoneId SYDNEY = ZoneId.of("Australia/Sydney");

    private final Iso20022MessageRepository messageRepository;

    public Iso20022MessageService(Iso20022MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Transactional
    public Iso20022Message buildPacs008(NppPayment payment) {
        try {
            String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

            MxPacs00800108 mx = new MxPacs00800108();
            FIToFICustomerCreditTransferV08 doc = new FIToFICustomerCreditTransferV08();
            mx.setFIToFICstmrCdtTrf(doc);

            // Group Header
            GroupHeader93 grpHdr = new GroupHeader93();
            grpHdr.setMsgId(msgId);
            grpHdr.setCreDtTm(OffsetDateTime.now(SYDNEY));
            grpHdr.setNbOfTxs("1");
            grpHdr.setTtlIntrBkSttlmAmt(activeAmount(payment.getAmount()));
            grpHdr.setIntrBkSttlmDt(LocalDate.now(SYDNEY));

            // Settlement Instruction - CLRG for NPP/FSS
            SettlementInstruction7 sttlmInf = new SettlementInstruction7();
            sttlmInf.setSttlmMtd(SettlementMethod1Code.CLRG);
            grpHdr.setSttlmInf(sttlmInf);
            doc.setGrpHdr(grpHdr);

            // Credit Transfer Transaction
            CreditTransferTransaction39 txn = new CreditTransferTransaction39();

            // Payment Identification
            PaymentIdentification7 pmtId = new PaymentIdentification7();
            pmtId.setInstrId(payment.getPaymentId());
            pmtId.setEndToEndId(payment.getEndToEndId());
            pmtId.setTxId(payment.getPaymentId());
            txn.setPmtId(pmtId);

            // Amount
            txn.setIntrBkSttlmAmt(activeAmount(payment.getAmount()));

            // Debtor Agent
            BranchAndFinancialInstitutionIdentification6 dbtrAgt = new BranchAndFinancialInstitutionIdentification6();
            FinancialInstitutionIdentification18 dbtrFi = new FinancialInstitutionIdentification18();
            dbtrFi.setBICFI(payment.getDebtorAgent().getBic());
            dbtrAgt.setFinInstnId(dbtrFi);
            txn.setDbtrAgt(dbtrAgt);

            // Creditor Agent
            BranchAndFinancialInstitutionIdentification6 cdtrAgt = new BranchAndFinancialInstitutionIdentification6();
            FinancialInstitutionIdentification18 cdtrFi = new FinancialInstitutionIdentification18();
            cdtrFi.setBICFI(payment.getCreditorAgent().getBic());
            cdtrAgt.setFinInstnId(cdtrFi);
            txn.setCdtrAgt(cdtrAgt);

            // Debtor
            PartyIdentification135 dbtr = new PartyIdentification135();
            dbtr.setNm(payment.getDebtorAccount().getAccountName());
            txn.setDbtr(dbtr);

            // Creditor
            PartyIdentification135 cdtr = new PartyIdentification135();
            cdtr.setNm(payment.getCreditorAccount().getAccountName());
            txn.setCdtr(cdtr);

            // Remittance Information
            if (payment.getRemittanceInfo() != null && !payment.getRemittanceInfo().isEmpty()) {
                RemittanceInformation16 rmtInf = new RemittanceInformation16();
                rmtInf.addUstrd(payment.getRemittanceInfo());
                txn.setRmtInf(rmtInf);
            }

            doc.addCdtTrfTxInf(txn);

            String xml = mx.message();
            log.info("Built pacs.008 message {} for payment {}", msgId, payment.getPaymentId());

            return persistMessage(Iso20022MessageType.PACS_008, msgId, xml, "OUTBOUND",
                    payment.getDebtorAgent().getBic(), payment.getCreditorAgent().getBic(), payment);

        } catch (Exception e) {
            log.error("Failed to build pacs.008 for payment {}: {}", payment.getPaymentId(), e.getMessage(), e);
            return buildFallbackPacs008(payment);
        }
    }

    @Transactional
    public Iso20022Message buildPacs002(NppPayment payment, boolean accepted, String rejectReason) {
        try {
            String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

            MxPacs00200110 mx = new MxPacs00200110();
            FIToFIPaymentStatusReportV10 doc = new FIToFIPaymentStatusReportV10();
            mx.setFIToFIPmtStsRpt(doc);

            // Group Header
            GroupHeader91 grpHdr = new GroupHeader91();
            grpHdr.setMsgId(msgId);
            grpHdr.setCreDtTm(OffsetDateTime.now(SYDNEY));
            doc.setGrpHdr(grpHdr);

            // Transaction status
            PaymentTransaction110 txInf = new PaymentTransaction110();
            txInf.setOrgnlEndToEndId(payment.getEndToEndId());

            if (accepted) {
                txInf.setTxSts("ACCP");
            } else {
                txInf.setTxSts("RJCT");
                if (rejectReason != null) {
                    StatusReasonInformation12 stsRsnInf = new StatusReasonInformation12();
                    stsRsnInf.addAddtlInf(rejectReason);
                    txInf.getStsRsnInf().add(stsRsnInf);
                }
            }

            doc.addTxInfAndSts(txInf);

            String xml = mx.message();
            log.info("Built pacs.002 message {} ({}) for payment {}",
                    msgId, accepted ? "ACCP" : "RJCT", payment.getPaymentId());

            return persistMessage(Iso20022MessageType.PACS_002, msgId, xml, "INBOUND",
                    payment.getCreditorAgent().getBic(), payment.getDebtorAgent().getBic(), payment);

        } catch (Exception e) {
            log.error("Failed to build pacs.002 for payment {}: {}", payment.getPaymentId(), e.getMessage(), e);
            return buildFallbackPacs002(payment, accepted, rejectReason);
        }
    }

    @Transactional
    public Iso20022Message buildPacs004(NppPayment payment, String returnReason) {
        try {
            String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();

            MxPacs00400109 mx = new MxPacs00400109();
            PaymentReturnV09 doc = new PaymentReturnV09();
            mx.setPmtRtr(doc);

            // Group Header
            GroupHeader90 grpHdr = new GroupHeader90();
            grpHdr.setMsgId(msgId);
            grpHdr.setCreDtTm(OffsetDateTime.now(SYDNEY));
            grpHdr.setNbOfTxs("1");
            grpHdr.setTtlRtrdIntrBkSttlmAmt(activeAmount(payment.getAmount()));
            grpHdr.setIntrBkSttlmDt(LocalDate.now(SYDNEY));

            SettlementInstruction7 sttlmInf = new SettlementInstruction7();
            sttlmInf.setSttlmMtd(SettlementMethod1Code.CLRG);
            grpHdr.setSttlmInf(sttlmInf);
            doc.setGrpHdr(grpHdr);

            // Return Transaction - PaymentTransaction112 is the correct type for pacs.004.001.09
            PaymentTransaction112 txInf = new PaymentTransaction112();
            txInf.setOrgnlEndToEndId(payment.getEndToEndId());
            txInf.setRtrdIntrBkSttlmAmt(activeAmount(payment.getAmount()));

            if (returnReason != null) {
                PaymentReturnReason6 rtrRsnInf = new PaymentReturnReason6();
                rtrRsnInf.addAddtlInf(returnReason);
                txInf.addRtrRsnInf(rtrRsnInf);
            }

            doc.addTxInf(txInf);

            String xml = mx.message();
            log.info("Built pacs.004 message {} for payment {}", msgId, payment.getPaymentId());

            return persistMessage(Iso20022MessageType.PACS_004, msgId, xml, "OUTBOUND",
                    payment.getCreditorAgent().getBic(), payment.getDebtorAgent().getBic(), payment);

        } catch (Exception e) {
            log.error("Failed to build pacs.004 for payment {}: {}", payment.getPaymentId(), e.getMessage(), e);
            return buildFallbackPacs004(payment, returnReason);
        }
    }

    public List<Iso20022Message> findByPayment(NppPayment payment) {
        return messageRepository.findByPayment(payment);
    }

    public List<Iso20022Message> findAll() {
        return messageRepository.findAllByOrderByCreatedAtDesc();
    }

    public Iso20022Message findById(Long id) {
        return messageRepository.findById(id).orElse(null);
    }

    private Iso20022Message persistMessage(Iso20022MessageType type, String msgId, String xml,
                                           String direction, String senderBic, String receiverBic,
                                           NppPayment payment) {
        Iso20022Message msg = new Iso20022Message();
        msg.setMessageType(type);
        msg.setMessageId(msgId);
        msg.setXmlContent(xml);
        msg.setDirection(direction);
        msg.setSenderBic(senderBic);
        msg.setReceiverBic(receiverBic);
        msg.setPayment(payment);
        return messageRepository.save(msg);
    }

    // Fallback methods generate well-formed XML when Prowide classes encounter issues
    private Iso20022Message buildFallbackPacs008(NppPayment payment) {
        String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String xml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.008.001.08">
                  <FIToFICstmrCdtTrf>
                    <GrpHdr>
                      <MsgId>%s</MsgId>
                      <CreDtTm>%s</CreDtTm>
                      <NbOfTxs>1</NbOfTxs>
                      <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                    </GrpHdr>
                    <CdtTrfTxInf>
                      <PmtId>
                        <InstrId>%s</InstrId>
                        <EndToEndId>%s</EndToEndId>
                      </PmtId>
                      <IntrBkSttlmAmt Ccy="AUD">%s</IntrBkSttlmAmt>
                      <DbtrAgt><FinInstnId><BICFI>%s</BICFI></FinInstnId></DbtrAgt>
                      <CdtrAgt><FinInstnId><BICFI>%s</BICFI></FinInstnId></CdtrAgt>
                      <Dbtr><Nm>%s</Nm></Dbtr>
                      <Cdtr><Nm>%s</Nm></Cdtr>
                      <RmtInf><Ustrd>%s</Ustrd></RmtInf>
                    </CdtTrfTxInf>
                  </FIToFICstmrCdtTrf>
                </Document>""",
                msgId, LocalDateTime.now(), payment.getPaymentId(), payment.getEndToEndId(),
                payment.getAmount().toPlainString(),
                payment.getDebtorAgent().getBic(), payment.getCreditorAgent().getBic(),
                payment.getDebtorAccount().getAccountName(), payment.getCreditorAccount().getAccountName(),
                payment.getRemittanceInfo() != null ? payment.getRemittanceInfo() : "");

        return persistMessage(Iso20022MessageType.PACS_008, msgId, xml, "OUTBOUND",
                payment.getDebtorAgent().getBic(), payment.getCreditorAgent().getBic(), payment);
    }

    private Iso20022Message buildFallbackPacs002(NppPayment payment, boolean accepted, String rejectReason) {
        String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String statusCode = accepted ? "ACCP" : "RJCT";
        String reasonXml = !accepted && rejectReason != null
                ? String.format("<StsRsnInf><AddtlInf>%s</AddtlInf></StsRsnInf>", rejectReason) : "";
        String xml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.002.001.10">
                  <FIToFIPmtStsRpt>
                    <GrpHdr>
                      <MsgId>%s</MsgId>
                      <CreDtTm>%s</CreDtTm>
                    </GrpHdr>
                    <TxInfAndSts>
                      <OrgnlEndToEndId>%s</OrgnlEndToEndId>
                      <TxSts>%s</TxSts>
                      %s
                    </TxInfAndSts>
                  </FIToFIPmtStsRpt>
                </Document>""",
                msgId, LocalDateTime.now(), payment.getEndToEndId(), statusCode, reasonXml);

        return persistMessage(Iso20022MessageType.PACS_002, msgId, xml, "INBOUND",
                payment.getCreditorAgent().getBic(), payment.getDebtorAgent().getBic(), payment);
    }

    private Iso20022Message buildFallbackPacs004(NppPayment payment, String returnReason) {
        String msgId = "MSG" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        String reasonXml = returnReason != null
                ? String.format("<RtrRsnInf><AddtlInf>%s</AddtlInf></RtrRsnInf>", returnReason) : "";
        String xml = String.format("""
                <?xml version="1.0" encoding="UTF-8"?>
                <Document xmlns="urn:iso:std:iso:20022:tech:xsd:pacs.004.001.09">
                  <PmtRtr>
                    <GrpHdr>
                      <MsgId>%s</MsgId>
                      <CreDtTm>%s</CreDtTm>
                      <NbOfTxs>1</NbOfTxs>
                      <SttlmInf><SttlmMtd>CLRG</SttlmMtd></SttlmInf>
                    </GrpHdr>
                    <TxInf>
                      <OrgnlEndToEndId>%s</OrgnlEndToEndId>
                      <RtrdIntrBkSttlmAmt Ccy="AUD">%s</RtrdIntrBkSttlmAmt>
                      %s
                    </TxInf>
                  </PmtRtr>
                </Document>""",
                msgId, LocalDateTime.now(), payment.getEndToEndId(),
                payment.getAmount().toPlainString(), reasonXml);

        return persistMessage(Iso20022MessageType.PACS_004, msgId, xml, "OUTBOUND",
                payment.getCreditorAgent().getBic(), payment.getDebtorAgent().getBic(), payment);
    }

    private ActiveCurrencyAndAmount activeAmount(BigDecimal amount) {
        ActiveCurrencyAndAmount amt = new ActiveCurrencyAndAmount();
        amt.setValue(amount);
        amt.setCcy("AUD");
        return amt;
    }
}
