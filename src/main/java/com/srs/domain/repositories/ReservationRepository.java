package com.srs.domain.repositories;

import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Reservation;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ReservationRepository extends R2dbcRepository<Reservation, Long> {

    Flux<Reservation> findAll();

    Flux<Reservation> findReservationsByAmenityType(AmenityType amenityType);

    Flux<Reservation> findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            LocalTime betweenStart,
            LocalTime betweenEnd
    );

    Mono<Reservation> findReservationByReservationName(String reservationName);
}
