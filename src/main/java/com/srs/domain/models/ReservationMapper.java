package com.srs.domain.models;

import com.srs.domain.models.dto.ReservationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    static AmenityType stringToAmenityType(String amenityType) {
        return AmenityType.fromString(amenityType);
    }

    static String amenityTypeToString(AmenityType amenityType) {
        return amenityType.toString();
    }

    @Mappings({
            @Mapping(target = "reservationDate", source = "reservationDate"),
            @Mapping(target = "startTime", source = "startTime"),
            @Mapping(target = "endTime", source = "endTime"),
            @Mapping(target = "amenityType", source = "amenityType"),
            @Mapping(target = "username", source = "username"),
            @Mapping(target = "userId", source = "userId")
    })
    ReservationDTO toDto(Reservation reservation);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "reservationDate", source = "reservationDate"),
            @Mapping(target = "startTime", source = "startTime"),
            @Mapping(target = "endTime", source = "endTime"),
            @Mapping(target = "amenityType", source = "amenityType"),
            @Mapping(target = "username", source = "username"),
            @Mapping(target = "userId", source = "userId")
    })
    Reservation toEntity(ReservationDTO reservationDTO);
}
