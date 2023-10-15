package com.srs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "\"user\"")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

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
  private String fullName;

  @Column(nullable = false, unique = true)
  private String username;

  @Column
  private String passwordHash;

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

    public User(String fullName, String username, String passwordHash) {
        this.fullName = fullName;
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
