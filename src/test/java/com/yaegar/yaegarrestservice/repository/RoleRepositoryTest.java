package com.yaegar.yaegarrestservice.repository;

import com.yaegar.yaegarrestservice.model.Role;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.junit4.SpringRunner;

import static com.yaegar.yaegarrestservice.model.Role.AUTHORITY_USER;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

@RunWith(SpringRunner.class)
@DataJpaTest
public class RoleRepositoryTest {
    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    public void whenFindByAuthority_thenReturnRole() {
        //given
        Role expectedRole = new Role(AUTHORITY_USER);
        entityManager.persist(expectedRole);

        //when
        Role actualRole = roleRepository.findByAuthority(AUTHORITY_USER).get();

        //then
        assertThat(actualRole.getAuthority(), is(expectedRole.getAuthority()));
    }
}