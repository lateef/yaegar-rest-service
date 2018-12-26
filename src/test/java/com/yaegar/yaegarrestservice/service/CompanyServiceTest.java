package com.yaegar.yaegarrestservice.service;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import com.yaegar.yaegarrestservice.repository.CompanyRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
public class CompanyServiceTest {
    @MockBean
    private CompanyRepository companyRepository;

    private CompanyService companyService;

    @Before
    public void setUp() {
        companyService = new CompanyService(companyRepository);
    }

    @Test
    public void whenAddCompany_thenACompanyIsSaved() throws IOException {
        //given
        Company company = new Company("Yaegar");
        Company expectedCompany = new Company("Yaegar");
         expectedCompany.setId(1L);
        when(companyRepository.save(company)).thenReturn(expectedCompany);

        //when
        User user = new User();
        Company actualCompany = companyService.addCompany(company, user);

        //then
        assertThat(actualCompany, is(sameBeanAs(expectedCompany)));
    }
}