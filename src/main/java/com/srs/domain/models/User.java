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
import java.util.Set;
import java.util.stream.Stream;

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
    @Column("roles")
    private String roles;
    @Column("date_created")
    private OffsetDateTime dateCreated;

    @Column("last_updated")
    private OffsetDateTime lastUpdated;

    //UserDetails implementations
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.of(roles.split(", ")).map(SimpleGrantedAuthority::new)
                .toList();
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    public Roles getRoleEnum() {
        return Roles.valueOf(this.roles);
    }

    public void setRoleEnum(Roles role) {
        this.roles = role.toString();
    }
}