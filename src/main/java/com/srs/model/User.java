package com.srs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
  name = "*/user*/",
  uniqueConstraints = { @UniqueConstraint(columnNames = { "username" }) }
)
public class User implements UserDetails {

  @Id
  @Column(nullable = false, updatable = false)
  @SequenceGenerator(
    name = "primary_sequence",
    sequenceName = "primary_sequence",
    allocationSize = 1,
    initialValue = 1
  )
  @GeneratedValue(
    strategy = GenerationType.SEQUENCE,
    generator = "primary_sequence"
  )
  private Long id;

  @Column(nullable = false, unique = true)
  private String fullname;

  @Column(nullable = false, unique = true)
  private String username;

  @Column
  private String country;

  @Column
  private String password;

  @Builder.Default
  @Enumerated(EnumType.STRING)
  Role role = Role.USER;

  @Builder.Default
  @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
  private Set<Reservation> reservations = new HashSet<>();

  @Column(nullable = true, updatable = false)
  private OffsetDateTime dateCreated;

  @Column(nullable = true)
  private OffsetDateTime lastUpdated;

  @PrePersist
  public void prePersist() {
    dateCreated = OffsetDateTime.now();
    lastUpdated = dateCreated;
  }

  @PreUpdate
  public void preUpdate() {
    lastUpdated = OffsetDateTime.now();
  }

  //UserDetails implementations
  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority((role.name())));
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
}
