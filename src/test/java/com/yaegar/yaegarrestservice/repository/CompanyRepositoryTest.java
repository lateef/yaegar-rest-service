package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Company;
import com.yaegar.yaegarrestservice.model.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CompanyRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CompanyRepository companyRepository;

    @Test
    public void whenFindUuid_thenReturnCompany() {
        //given
        Company expectedCompany = new Company("Yaegar");
        String uuid = "uuiduuiduuiduuiduuiduuiduuiduuiduuid";
        expectedCompany.setUuid(uuid);
        entityManager.persist(expectedCompany);
        entityManager.flush();

        //when
        Company actualCompany = companyRepository.findByUuid(uuid).get();

        //then
        assertThat(actualCompany, sameBeanAs(expectedCompany));
    }

    @Test
    public void whenFindByEmployeesIn_thenReturnCompany() {
        //given
        User employee = new User();
        employee.setPhoneNumber("123456789");
        employee.setAcceptedTerms(true);
        employee.setUuid(UUID.randomUUID().toString());
        entityManager.persist(employee);
        entityManager.flush();
        Set<User> employees = Collections.singleton(employee);

        Company expectedCompany = new Company("Yaegar");
        String uuid = "uuiduuiduuiduuiduuiduuiduuiduuiduuid";
        expectedCompany.setUuid(uuid);
        expectedCompany.setEmployees(employees);
        entityManager.persist(expectedCompany);
        entityManager.flush();

        //when
        Company actualCompany = companyRepository.findByEmployeesIn(Collections.singleton(employee)).stream().findFirst().get();

        //then
        assertThat(actualCompany, sameBeanAs(expectedCompany));
    }
}