package com.srs.domain.repositories;

import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Capacity;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface CapacityRepository extends R2dbcRepository<Capacity, Long> {
    @Query("SELECT * FROM capacities WHERE amenity_type = :amenityType")
    Mono<Capacity> findByAmenityType(AmenityType amenityType);

    @Query("SELECT COUNT(*) > 0 FROM capacities WHERE amenity_type = :amenityType")
    Mono<Boolean> existsByAmenityType(AmenityType amenityType);
}
