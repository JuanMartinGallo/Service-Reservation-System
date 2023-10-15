package com.srs.repository;

import com.srs.model.AmenityType;
import com.srs.model.Capacity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CapacityRepository extends JpaRepository<Capacity, Long> {
    Capacity findByAmenityType(AmenityType amenityType);
}
