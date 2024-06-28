package com.srs.domain.services;

import com.srs.domain.models.Reservation;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface ReservationService {
    Flux<Reservation> findAll();

    Mono<Reservation> getById(final Long id);

    Mono<Long> createReservation(final Reservation reservation);

    Mono<Void> updateReservation(final Long id, final Reservation reservation);

    Mono<Void> deleteReservation(final Long id);

    Mono<Reservation> getReservationByReservationDate(String reservationDate);
}
