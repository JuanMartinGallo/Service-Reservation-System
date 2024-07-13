package com.srs.domain.models;

import com.srs.domain.models.dto.ReservationDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mappings({
            @Mapping(target = "reservationDate", source = "reservationDate"),
            @Mapping(target = "startTime", source = "startTime"),
            @Mapping(target = "endTime", source = "endTime"),
            @Mapping(target = "amenityType", source = "amenityType")
    })
    ReservationDTO toDto(Reservation reservation);

    @Mappings({
            @Mapping(target = "id", ignore = true),
            @Mapping(target = "reservationDate", source = "reservationDate"),
            @Mapping(target = "startTime", source = "startTime"),
            @Mapping(target = "endTime", source = "endTime"),
            @Mapping(target = "amenityType", source = "amenityType")
    })
    Reservation toEntity(ReservationDTO reservationDTO);
}