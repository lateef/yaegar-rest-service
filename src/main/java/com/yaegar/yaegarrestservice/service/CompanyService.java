package com.yaegar.yaegarrestservice.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaegar.yaegarrestservice.model.*;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.enums.LocationType.STORE;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;

@Slf4j
@RequiredArgsConstructor
@Service
public class CompanyService {
    private final AccountRepository accountRepository;
    private final CompanyRepository companyRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    public Company addCompany(Company company, User user) throws IOException {
        if (company.getId() != null) {
            return company;
        }
        final Company company1 = new Company(company.getName().trim());
        company1.setOwners(singleton(user));
        company1.setEmployees(singleton(user));
        ChartOfAccounts chartOfAccounts = new ChartOfAccounts();
        company1.setChartOfAccounts(chartOfAccounts);
        company1.setCountry(user.getCountry());

        final Location location = new Location();
        location.setLocationType(STORE);
        location.setName(String.join(" ", company1.getName(), STORE.name()));
        location.setCode(UUID.randomUUID().toString());
        company1.setLocations(singletonList(location));
        Company company2 = companyRepository.save(company1);

        List<Account> primaryCompanyAccounts = accountRepository.saveAll(createPrimaryCompanyAccount(company2.getChartOfAccounts()));
        List<Account> companyAccounts = createCompanyAccount(primaryCompanyAccounts, company2.getChartOfAccounts());
        List<Account> companyAccounts2 = accountRepository.saveAll(companyAccounts);
        primaryCompanyAccounts.addAll(companyAccounts2);
        chartOfAccounts.setAccounts(new HashSet<>(primaryCompanyAccounts));

        setSuperUserRoleOnCompanyCreate(user, company2);
        return company2;
    }

    public Optional<Company> findById(UUID id) {
        return companyRepository.findById(id);
    }

    public List<Company> getCompaniesByEmployeesIn(List<User> employees) {
        return companyRepository.findByEmployeesIn(employees);
    }

    public boolean userCanAccessChartOfAccounts(User user, UUID chartOfAccountsId) {
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
            log.error("Could not read file path {}", accountFilepath, e);
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
