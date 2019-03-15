package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Account;
import com.yaegar.yaegarrestservice.model.ChartOfAccounts;
import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.Role;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.AccountRepository;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import com.yaegar.yaegarrestservice.repository.RoleRepository;
import com.yaegar.yaegarrestservice.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CompanyServiceTest {
    @MockBean
    private AccountRepository accountRepository;

    @MockBean
    private CompanyRepository companyRepository;

    @MockBean
    private RoleRepository roleRepository;

    @MockBean
    private UserRepository userRepository;

    private CompanyService companyService;

    @Before
    public void setUp() {
        companyService = new CompanyService(accountRepository, companyRepository, roleRepository, userRepository);
    }

    @Test
    public void whenAddCompany_thenACompanyIsSaved() throws IOException {
        //given
        Company company = new Company("Yaegar");
        Company expectedCompany = new Company("Yaegar");
        expectedCompany.setId(1L);
        ChartOfAccounts expectedChartOfAccounts = new ChartOfAccounts();
        expectedChartOfAccounts.setId(1L);
        expectedCompany.setChartOfAccounts(expectedChartOfAccounts);
        when(companyRepository.save(company)).thenReturn(expectedCompany);
        when(accountRepository.saveAll(ArgumentMatchers.any())).thenReturn(Collections.emptyList());
        when(roleRepository.save(ArgumentMatchers.any())).thenReturn(new Role());

        //when
        User user = new User();
        user.setRoles(new HashSet<>());
        Company actualCompany = companyService.addCompany(company, user);

        //then
        assertThat(actualCompany, is(sameBeanAs(expectedCompany)));
    }

    @Test
    public void readChartOfAccountsTemplateFromFile() throws Exception {
        //when
        final List<Account> accounts = companyService.readChartOfAccountsTemplateFromFile();

        //then
        assertThat(accounts.size(), is(36));
    }
}