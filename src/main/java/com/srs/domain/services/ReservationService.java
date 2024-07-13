package com.srs.domain.services;

import com.srs.domain.models.dto.ReservationDTO;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public interface ReservationService {
    Flux<ReservationDTO> findAll();

    Mono<ReservationDTO> getById(final Long id);

    Mono<Long> createReservation(final ReservationDTO reservationDTO);

    Mono<Void> updateReservation(final Long id, final ReservationDTO reservation);

    Mono<Void> deleteReservation(final Long id);

    Mono<ReservationDTO> getReservationByReservationDate(String reservationDate);
}
