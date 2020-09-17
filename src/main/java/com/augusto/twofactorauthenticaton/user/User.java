package com.augusto.twofactorauthenticaton.user;

import com.augusto.twofactorauthenticaton.user.role.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Collection;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(insertable = false, updatable = false)
    private Long id;

    private String name;
    private String username;
    private String password;
    private String phone;

    @ManyToOne(fetch = FetchType.EAGER)
    private Role role;

    private boolean twoFactorAuthentication;
    private String code;
    private LocalDateTime expiration;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.role != null ? AuthorityUtils.createAuthorityList(this.role.getName()) : AuthorityUtils.NO_AUTHORITIES;
    }

    @Override
    public String getUsername() {
        return this.username;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
