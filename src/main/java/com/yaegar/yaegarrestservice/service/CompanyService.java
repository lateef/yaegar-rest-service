package com.yaegar.yaegarrestservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.Role;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CompanyService.class);

    private AccountRepository accountRepository;
    private CompanyRepository companyRepository;
    private RoleRepository roleRepository;
    private UserRepository userRepository;

    public CompanyService(
            AccountRepository accountRepository,
            CompanyRepository companyRepository,
            RoleRepository roleRepository,
            UserRepository userRepository
    ) {
        this.companyRepository = companyRepository;
        this.accountRepository = accountRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
    }

    public Company addCompany(Company company, User user) throws IOException {
        if (company.getId() != null) {
            return company;
        }
        company.setName(company.getName().trim());
        company.setOwners(Collections.singleton(user));
        company.setEmployees(Collections.singleton(user));
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
        company.setChartOfAccounts(chartOfAccounts);
        Company company1 = companyRepository.save(company);

        List<Account> companyAccounts = createCompanyAccount(
                createPrimaryCompanyAccount(company1.getChartOfAccounts()),
                company1.getChartOfAccounts()
        );
        List<Account> companyAccounts2 = accountRepository.saveAll(companyAccounts);
        chartOfAccounts.setAccounts(new HashSet<>(companyAccounts2));

        setSuperUserRoleOnCompanyCreate(user, company1);
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

    public List<Account> readChartOfAccountsTemplateFromFile() throws IOException {
        String accountFilepath = "/chartOfAccountsTemplate.json";
        try {
            final InputStream resourceAsStream = getClass().getResourceAsStream(accountFilepath);
            return new ObjectMapper().readValue(resourceAsStream, new TypeReference<List<Account>>() {
            });
        } catch (IOException e) {
            LOGGER.error("Could not read file path {}", accountFilepath, e);
            throw new IOException();
        }
    }

    private List<Account> createPrimaryCompanyAccount(ChartOfAccounts chartOfAccounts) throws IOException {
        List<Account> companyAccounts = readChartOfAccountsTemplateFromFile();

        return companyAccounts.stream()
                .filter(companyAccount -> (companyAccount.getCode() % 1000000) == 0)
                .map(companyAccount -> {
                    companyAccount.setChartOfAccounts(chartOfAccounts);
                    return companyAccount;
                })
                .collect(Collectors.toList());
    }

    private List<Account> createCompanyAccount(List<Account> primaryAccounts, ChartOfAccounts chartOfAccounts) throws IOException {
        List<Account> companyAccounts = readChartOfAccountsTemplateFromFile();

        return companyAccounts.stream()
                .filter(companyAccount -> (companyAccount.getCode() % 1000000) != 0)
                .map(companyAccount -> {
                    int parentCode = (companyAccount.getCode() / 1000000) * 1000000;
                    Account primaryAccount = primaryAccounts.stream()
                            .filter(account -> parentCode == account.getCode())
                            .findFirst()
                            .orElseThrow(NullPointerException::new);

                    companyAccount.setParentId(primaryAccount.getId());
                    companyAccount.setChartOfAccounts(chartOfAccounts);
                    return companyAccount;
                })
                .collect(Collectors.toList());
    }

    private void setSuperUserRoleOnCompanyCreate(User user, Company company1) {
        final Role superUserRole = new Role("ROLE_SUPER_USER_" + company1.getId());
        final Role savedSuperUserRole = roleRepository.save(superUserRole);
        user.getRoles().add(savedSuperUserRole);
        userRepository.save(user);
    }
}
