package com.nick.npp.config;

import com.nick.npp.model.*;
import com.nick.npp.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final NppParticipantRepository participantRepo;
    private final BankAccountRepository accountRepo;
    private final PayIdRepository payIdRepo;
    private final PayToMandateRepository mandateRepo;

    public DataInitializer(NppParticipantRepository participantRepo,
                           BankAccountRepository accountRepo,
                           PayIdRepository payIdRepo,
                           PayToMandateRepository mandateRepo) {
        this.participantRepo = participantRepo;
        this.accountRepo = accountRepo;
        this.payIdRepo = payIdRepo;
        this.mandateRepo = mandateRepo;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Initializing NPP demo data...");

        // 4 Major Australian Banks
        NppParticipant cba = participantRepo.save(
                new NppParticipant("Commonwealth Bank of Australia", "CBA", "CTBAAU2S", "062-000", new BigDecimal("50000000.00")));
        NppParticipant nab = participantRepo.save(
                new NppParticipant("National Australia Bank", "NAB", "NATAAU33", "083-000", new BigDecimal("48000000.00")));
        NppParticipant anz = participantRepo.save(
                new NppParticipant("Australia and New Zealand Banking Group", "ANZ", "ANZBAU3M", "012-000", new BigDecimal("47000000.00")));
        NppParticipant westpac = participantRepo.save(
                new NppParticipant("Westpac Banking Corporation", "Westpac", "WPACAU2S", "032-000", new BigDecimal("45000000.00")));

        // Bank Accounts - Personal and Business across all banks
        // CBA accounts
        BankAccount johnCba = accountRepo.save(
                new BankAccount("12345678", "062-000", "John Smith", new BigDecimal("15420.50"), cba));
        BankAccount sarahCba = accountRepo.save(
                new BankAccount("87654321", "062-000", "Sarah Johnson", new BigDecimal("8750.00"), cba));
        BankAccount acmeCba = accountRepo.save(
                new BankAccount("11112222", "062-000", "ACME Pty Ltd", new BigDecimal("250000.00"), cba));

        // NAB accounts
        BankAccount mikeNab = accountRepo.save(
                new BankAccount("22334455", "083-000", "Mike Wilson", new BigDecimal("32100.75"), nab));
        BankAccount techNab = accountRepo.save(
                new BankAccount("55667788", "083-000", "TechCorp Australia", new BigDecimal("180000.00"), nab));

        // ANZ accounts
        BankAccount emmaAnz = accountRepo.save(
                new BankAccount("33445566", "012-000", "Emma Davis", new BigDecimal("5200.30"), anz));
        BankAccount greenAnz = accountRepo.save(
                new BankAccount("66778899", "012-000", "Green Energy Solutions", new BigDecimal("420000.00"), anz));

        // Westpac accounts
        BankAccount jamesWpc = accountRepo.save(
                new BankAccount("44556677", "032-000", "James Brown", new BigDecimal("18900.00"), westpac));
        BankAccount ozWpc = accountRepo.save(
                new BankAccount("99887766", "032-000", "OzTrade Imports", new BigDecimal("95000.00"), westpac));

        // PayIDs - Phone
        payIdRepo.save(new PayId(PayIdType.PHONE, "+61412345678", "John S", true, johnCba));
        payIdRepo.save(new PayId(PayIdType.PHONE, "+61498765432", "Mike W", true, mikeNab));
        payIdRepo.save(new PayId(PayIdType.PHONE, "+61423456789", "Emma D", true, emmaAnz));

        // PayIDs - Email
        payIdRepo.save(new PayId(PayIdType.EMAIL, "sarah.j@email.com", "Sarah Johnson", true, sarahCba));
        payIdRepo.save(new PayId(PayIdType.EMAIL, "james.b@email.com", "James Brown", true, jamesWpc));

        // PayIDs - ABN
        payIdRepo.save(new PayId(PayIdType.ABN, "51824753556", "ACME Pty Ltd", true, acmeCba));
        payIdRepo.save(new PayId(PayIdType.ABN, "12345678901", "TechCorp Australia", true, techNab));
        payIdRepo.save(new PayId(PayIdType.ABN, "98765432100", "Green Energy Solutions", true, greenAnz));

        // PayTo Mandates
        PayToMandate activeMandate = new PayToMandate();
        activeMandate.setMandateId(UUID.randomUUID().toString());
        activeMandate.setDescription("Monthly electricity bill");
        activeMandate.setMaximumAmount(new BigDecimal("500.00"));
        activeMandate.setFrequency("MONTHLY");
        activeMandate.setStatus(MandateStatus.ACTIVE);
        activeMandate.setValidFrom(LocalDate.now().minusMonths(3));
        activeMandate.setValidTo(LocalDate.now().plusYears(1));
        activeMandate.setCreditorAccount(greenAnz);
        activeMandate.setDebtorAccount(johnCba);
        mandateRepo.save(activeMandate);

        PayToMandate pendingMandate = new PayToMandate();
        pendingMandate.setMandateId(UUID.randomUUID().toString());
        pendingMandate.setDescription("Gym membership subscription");
        pendingMandate.setMaximumAmount(new BigDecimal("120.00"));
        pendingMandate.setFrequency("MONTHLY");
        pendingMandate.setStatus(MandateStatus.PENDING);
        pendingMandate.setValidFrom(LocalDate.now());
        pendingMandate.setValidTo(LocalDate.now().plusYears(2));
        pendingMandate.setCreditorAccount(techNab);
        pendingMandate.setDebtorAccount(emmaAnz);
        mandateRepo.save(pendingMandate);

        log.info("NPP demo data initialized: 4 banks, 9 accounts, 8 PayIDs, 2 mandates");
    }
}
