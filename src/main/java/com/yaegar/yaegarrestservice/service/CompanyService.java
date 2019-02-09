package com.yaegar.yaegarrestservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.ClassLoader.getSystemResourceAsStream;

@Service
public class CompanyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    private CompanyRepository companyRepository;
    private AccountRepository accountRepository;

    public CompanyService(CompanyRepository companyRepository, AccountRepository accountRepository) {
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
    }

    public Company addCompany(Company company, User user) throws IOException {
        if (company.getId() != null) {
            return company;
        }
        company.setName(company.getName().trim());
        company.setCreatedBy(user.getId());
        company.setUpdatedBy(user.getId());
        company.setOwners(Collections.singleton(user));
        company.setEmployees(Collections.singleton(user));
        List<Account> primaryCompanyAccounts = createPrimaryCompanyAccount(user);
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts(primaryCompanyAccounts);
        company.setChartOfAccounts(chartOfAccounts);
        Company company1 = companyRepository.save(company);

        List<Account> companyAccounts = createCompanyAccount(
                primaryCompanyAccounts,
                company1.getChartOfAccounts().getId(),
                user
        );
        List<Account> companyAccounts2 = accountRepository.saveAll(companyAccounts);
        primaryCompanyAccounts.addAll(companyAccounts2);
        chartOfAccounts.setAccounts(primaryCompanyAccounts);
        return company1;
    }

    public Optional<Company> findById(Long id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompaniesByEmployeesIn(List<User> employees) {
        return companyRepository.findByEmployeesIn(employees);
    }

    public boolean userCanAccessChartOfAccounts(User user, Long chartOfAccountsId) {
        List<Company> companies = getCompaniesByEmployeesIn(Collections.singletonList(user));
        return companies.stream()
                .anyMatch(company -> company.getChartOfAccounts().getId().equals(chartOfAccountsId));
    }

    private List<Account> readChartOfAccountsTemplateFromFile() throws IOException {
        String accountFilepath = "chartOfAccountsTemplate.json";
        try {
            return new ObjectMapper().readValue(
                    getSystemResourceAsStream(accountFilepath), new TypeReference<List<Account>>() {
                    });
        } catch (IOException e) {
            LOGGER.error("Could not read file path {}", accountFilepath, e);
            throw new IOException();
        }
    }

    private List<Account> createPrimaryCompanyAccount(User user) throws IOException {
        List<Account> companyAccounts = readChartOfAccountsTemplateFromFile();

        return companyAccounts.stream()
                .filter(companyAccount -> (companyAccount.getCode() % 1000000) == 0)
                .map(companyAccount -> {
                    companyAccount.setCreatedBy(user.getId());
                    companyAccount.setUpdatedBy(user.getId());
                    return companyAccount;
                })
                .collect(Collectors.toList());
    }

    private List<Account> createCompanyAccount(List<Account> primaryAccounts, Long chartOfAccountsId, User user) throws IOException {
        List<Account> companyAccounts = readChartOfAccountsTemplateFromFile();

        return companyAccounts.stream()
                .filter(companyAccount -> (companyAccount.getCode() % 1000000) != 0)
                .map(companyAccount -> {
                    int parentCode = (companyAccount.getCode() / 1000000) * 1000000;
                    Account primaryAccount = primaryAccounts.stream()
                            .filter(account -> parentCode == account.getCode())
                            .findFirst()
                            .orElseThrow(NullPointerException::new);

                    companyAccount.setCreatedBy(user.getId());
                    companyAccount.setUpdatedBy(user.getId());
                    companyAccount.setParentId(primaryAccount.getId());
                    companyAccount.setAccountChartOfAccountsId(chartOfAccountsId);
                    return companyAccount;
                })
                .collect(Collectors.toList());
    }
}
