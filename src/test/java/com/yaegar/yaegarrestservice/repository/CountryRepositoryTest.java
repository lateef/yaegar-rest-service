package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Country;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static com.shazam.shazamcrest.matcher.Matchers.sameBeanAs;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class CountryRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    public void whenFindByCode_thenReturnCountry() {
        //given
        Country expectedCountry = new Country();
        expectedCountry.setName("Country");
        expectedCountry.setFullName("Federal Republic of Country");
        expectedCountry.setCode("ZZ");
        expectedCountry.setIso3("ZZZ");
        expectedCountry.setContinentCode("ZZ");
        entityManager.persist(expectedCountry);
        entityManager.flush();

        //when
        Country actualCountry = countryRepository.findByCode("ZZ").get();

        //then
        assertThat(actualCountry, sameBeanAs(expectedCountry));
    }

}