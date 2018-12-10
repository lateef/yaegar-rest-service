package com.yaegar.yaegarrestservice.model;

import com.yaegar.yaegarrestservice.audit.entity.AbstractEntity;
import org.hibernate.validator.constraints.Length;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
public class User extends AbstractEntity implements Serializable {
    private static final long serialVersionUID = 4857310005018510052L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UserID")
    private Long userId;

    @Length(max = 32)
    @Column(name = "FirstName", length = 32)
    private String firstName;

    @NotEmpty
    @Column(name = "PhoneNumber", unique = true, length = 15, nullable = false)
    private String phoneNumber;

    @NotEmpty
    @Length(min = 6, max = 128)
    @Column(name = "Password", nullable = false, length = 128)
    private String password;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "PhoneUserID", referencedColumnName = "UserID")
    private Set<Phone> phones;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "CountryID", referencedColumnName = "CountryID")
    private Country country;

    @Column(name = "DeletionDateTime")
    private LocalDateTime deletionDateTime;

    @Column(name = "AccountNonExpired")
    private boolean accountNonExpired;

    @Column(name = "AccountNonLocked")
    private boolean accountNonLocked;

    @Column(name = "CredentialsNonExpired")
    private boolean credentialsNonExpired;

    @Column(name = "Enabled")
    private boolean enabled;

    @Column(name = "FailedLoginAttempts")
    private int failedLoginAttempts;

    @AssertTrue
    @Column(name = "AcceptedTerms")
    private boolean acceptedTerms;

    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.DETACH)
    private Set<Role> roles;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Phone> getPhones() {
        return phones;
    }

    public void setPhones(Set<Phone> phones) {
        this.phones = phones;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public LocalDateTime getDeletionDateTime() {
        return deletionDateTime;
    }

    public void setDeletionDateTime(LocalDateTime deletionDateTime) {
        this.deletionDateTime = deletionDateTime;
    }

    public boolean isAccountNonExpired() {
        return accountNonExpired;
    }

    public void setAccountNonExpired(boolean accountNonExpired) {
        this.accountNonExpired = accountNonExpired;
    }

    public boolean isAccountNonLocked() {
        return accountNonLocked;
    }

    public void setAccountNonLocked(boolean accountNonLocked) {
        this.accountNonLocked = accountNonLocked;
    }

    public boolean isCredentialsNonExpired() {
        return credentialsNonExpired;
    }

    public void setCredentialsNonExpired(boolean credentialsNonExpired) {
        this.credentialsNonExpired = credentialsNonExpired;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        this.failedLoginAttempts = failedLoginAttempts;
    }

    public boolean isAcceptedTerms() {
        return acceptedTerms;
    }

    public void setAcceptedTerms(boolean acceptedTerms) {
        this.acceptedTerms = acceptedTerms;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void eraseCredentials() {
        password = null;
    }

    public Collection<GrantedAuthority> getAuthorities() {
        if (this.getRoles() == null) {
            return null;
        }
        return this.getRoles().stream().map(role -> new SimpleGrantedAuthority(role.getAuthority()))
                .collect(Collectors.toList());
    }
}
