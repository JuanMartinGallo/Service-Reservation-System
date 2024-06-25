package com.srs.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("capacities")
public class Capacity {

    @Id
    @Column("id")
    private Long id;

    @Column("amenity_type")
    private String amenityType;

    @Column("capacity")
    private int capacity;

    public Capacity(AmenityType amenityType, int capacity) {
        this.amenityType = amenityType.toString();
        this.capacity = capacity;
    }

    public AmenityType getAmenityTypeEnum() {
        return AmenityType.valueOf(this.amenityType);
    }

    public void setAmenityTypeEnum(AmenityType amenityType) {
        this.amenityType = amenityType.toString();
    }
}

