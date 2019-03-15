package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.yaegar.yaegarrestservice.model.Role.ANONYMOUS_USER;
import static java.util.Collections.singleton;

@Data
@EqualsAndHashCode(callSuper = true)
@Entity
public class User extends AbstractEntity {
    private static final long serialVersionUID = 4857310005018510052L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Length(max = 32)
    @Column(name = "first_name", length = 32)
    private String firstName;

    @NotEmpty
    @Column(name = "phone_number", unique = true, length = 17, nullable = false)
    private String phoneNumber;

    @Length(min = 6, max = 128)
    @Column(name = "password", nullable = false, length = 128)
    private String password;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "phone_user_id", referencedColumnName = "id")
    private List<Phone> phones;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    private Country country;

    @Column(name = "deleted_datetime")
    private LocalDateTime deletedDateTime;

    @Column(name = "account_non_expired")
    private boolean accountNonExpired;

    @Column(name = "account_non_locked")
    private boolean accountNonLocked;

    @Column(name = "credentials_non_expired")
    private boolean credentialsNonExpired;

    @Column(name = "enabled")
    private boolean enabled;

    @Column(name = "failed_login_attempts")
    private int failedLoginAttempts;

    @AssertTrue
    @Column(name = "accepted_terms")
    private boolean acceptedTerms;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private Set<Role> roles;

    public void eraseCredentials() {
        password = null;
        phones = phones.stream().map(phone -> {
            phone.setConfirmationCode(null);
            return phone;
        }).collect(Collectors.toList());
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (this.getRoles() == null) {
            return singleton(new SimpleGrantedAuthority(ANONYMOUS_USER));
        }
        return this.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }
}
