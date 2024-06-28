package com.srs.domain.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table("users")
public class User implements UserDetails {

    @Transient
    private final Set<Reservation> reservations = new HashSet<>();
    @Id
    @Column("id")
    private Long id;
    @Column("fullname")
    private String fullname;
    @Column("username")
    private String username;
    @Column("country")
    private String country;
    @Column("password")
    private String password;
    @Column("role")
    private String role;
    @Column("date_created")
    private OffsetDateTime dateCreated;

    @Column("last_updated")
    private OffsetDateTime lastUpdated;

    //UserDetails implementations
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority((role)));
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

    @Override
    public String getPassword() {
        return this.password;
    }

    public Role getRoleEnum() {
        return Role.valueOf(this.role);
    }

    public void setRoleEnum(Role role) {
        this.role = role.toString();
    }
}
