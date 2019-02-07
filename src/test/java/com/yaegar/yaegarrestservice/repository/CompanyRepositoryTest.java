package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Set;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CompanyRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void whenFindId_thenReturnCompany() {
        //given
        Company expectedCompany = new Company("Yaegar");
        entityManager.persist(expectedCompany);
        entityManager.flush();

        //when
        Company actualCompany = companyRepository.findById(1L).get();

        //then
        assertThat(actualCompany, sameBeanAs(expectedCompany));
    }

    @Test
    public void whenFindByEmployeesIn_thenReturnCompany() {
        //given
        User employee = new User();
        employee.setPhoneNumber("123456789");
        employee.setAcceptedTerms(true);
        entityManager.persist(employee);
        entityManager.flush();
        Set<User> employees = singleton(employee);

        Company expectedCompany = new Company("Yaegar");
        expectedCompany.setEmployees(employees);
        entityManager.persist(expectedCompany);
        entityManager.flush();

        //when
        Company actualCompany = companyRepository.findByEmployeesIn(singletonList(employee)).stream().findFirst().get();

        //then
        assertThat(actualCompany, sameBeanAs(expectedCompany));
    }
}