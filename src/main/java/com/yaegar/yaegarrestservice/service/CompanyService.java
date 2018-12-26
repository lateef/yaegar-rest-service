package com.yaegar.yaegarrestservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Ledger;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static java.lang.ClassLoader.getSystemResourceAsStream;

@Service
public class CompanyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    private CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company addCompany(Company company, User user) throws IOException {
        List<Ledger> companyLedgers = createCompanyLedger(user);
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts(companyLedgers);
        chartOfAccounts.setUuid(UUID.randomUUID().toString());
        company.setChartOfAccounts(chartOfAccounts);

        company.setCreatedBy(user.getId());
        company.setUpdatedBy(user.getId());
        company.setOwners(Collections.singleton(user));
        company.setEmployees(Collections.singleton(user));
        return companyRepository.save(company);
    }

    public Set<Company> getCompaniesByEmployeesIn(Set<User> employees) {
        return companyRepository.findByEmployeesIn(employees);
    }

    private List<Ledger> readLedgerTemplateFromFile() throws IOException {
        String ledgerFilepath = "ledgerTemplate.json";
        try {
            return new ObjectMapper().readValue(
                    getSystemResourceAsStream(ledgerFilepath), new TypeReference<List<Ledger>>() {
                    });
        } catch (IOException e) {
            LOGGER.error("Could not read file path {}", ledgerFilepath, e);
            throw new IOException();
        }
    }

    private List<Ledger> createCompanyLedger(User user) throws IOException {
        List<Ledger> ledgers1 = readLedgerTemplateFromFile();
        List<Ledger> ledgers2 = readLedgerTemplateFromFile();

        List<Ledger> companyLedgers = ledgers1.stream()
                .map(ledger1 -> {
                    ledger1.setUuid(UUID.randomUUID().toString());
                    ledger1.setCreatedBy(user.getId());
                    ledger1.setUpdatedBy(user.getId());
                    return ledger1;
                })
                .collect(Collectors.toList());

        return companyLedgers.stream()
                .map(companyLedger -> {
                    if (companyLedger.getParentUuid() != null) {
                        Ledger ledger = ledgers2.stream()
                                .filter(ledger2 -> companyLedger.getParentUuid().equals(ledger2.getUuid()))
                                .findFirst()
                                .orElseThrow(NullPointerException::new);

                        Ledger companyLedger2 = companyLedgers.stream()
                                .filter(ledger3 -> ledger.getName().equals(ledger3.getName()))
                                .findFirst()
                                .orElseThrow(NullPointerException::new);

                        companyLedger.setParentUuid(companyLedger2.getUuid());
                    }
                    return companyLedger;
                })
                .collect(Collectors.toList());
    }
}
