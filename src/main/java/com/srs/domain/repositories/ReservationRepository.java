package com.srs.domain.repositories;

import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Reservation;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

@Repository
public interface ReservationRepository extends R2dbcRepository<Reservation, Long> {

    @Query("SELECT * FROM reservations")
    Flux<Reservation> findAll();

    @Query("SELECT * FROM reservations WHERE amenity_type = :amenityType")
    Flux<Reservation> findReservationsByAmenityType(AmenityType amenityType);

    @Query("SELECT * FROM reservations WHERE reservation_date = :reservationDate AND ((start_time < :startTime AND end_time > :endTime) OR (start_time BETWEEN :betweenStart AND :betweenEnd))")
    Flux<Reservation> findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            LocalTime betweenStart,
            LocalTime betweenEnd
    );

    @Query("SELECT * FROM reservations WHERE reservation_date = :reservationDate")
    Mono<Reservation> findReservationByReservationDate(String reservationDate);

    @Query("SELECT * FROM reservations WHERE user_id = :userId")
    Flux<Reservation> findByUserId(Long userId);
}