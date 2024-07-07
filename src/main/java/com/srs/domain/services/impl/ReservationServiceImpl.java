package com.srs.domain.services.impl;

import com.srs.domain.exceptions.CapacityFullException;
import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Reservation;
import com.srs.domain.repositories.CapacityRepository;
import com.srs.domain.repositories.ReservationRepository;
import com.srs.domain.services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CapacityRepository capacityRepository;

    public Flux<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    public Mono<Reservation> getById(final Long id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")));
    }

    public Mono<Long> createReservation(final Reservation reservation) {
        return capacityRepository.findByAmenityType(AmenityType.valueOf(reservation.getAmenityType()))
                .flatMap(capacity -> reservationRepository
                        .findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
                                reservation.getReservationDate(),
                                reservation.getStartTime(),
                                reservation.getEndTime(),
                                reservation.getStartTime(),
                                reservation.getEndTime()
                        )
                        .count()
                        .flatMap(overlappingReservations -> {
                            if (overlappingReservations >= capacity.getCapacity()) {
                                return Mono.error(new CapacityFullException(
                                        "This amenity's capacity is full at desired time"));
                            } else {
                                return reservationRepository.save(reservation)
                                        .map(Reservation::getId);
                            }
                        })
                );
    }

    public Mono<Void> updateReservation(final Long id, final Reservation reservation) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(existingReservation -> {
                    reservation.setId(id);
                    return reservationRepository.save(reservation);
                })
                .then();
    }

    public Mono<Void> deleteReservation(final Long id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(reservationRepository::delete);
    }
    
    @Override
    public Mono<Reservation> getReservationByReservationDate(String reservationDate) {
        return reservationRepository.findReservationByReservationDate(reservationDate);
    }
}