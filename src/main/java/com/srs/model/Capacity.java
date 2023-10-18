package com.srs.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Capacity {

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
  @Enumerated(EnumType.STRING)
  private AmenityType amenityType;

  @Column(nullable = false)
  private int capacity;

  public Capacity(AmenityType amenityType, int capacity) {
    this.amenityType = amenityType;
    this.capacity = capacity;
  }
}
