package com.srs.service.impl;

import com.srs.exceptions.CapacityFullException;
import com.srs.model.Reservation;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import com.srs.service.ReservationService;
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

    /**
     * Retrieves all reservations.
     *
     * @return a Flux of Reservation objects representing all reservations
     */
    public Flux<Reservation> findAll() {
        return reservationRepository.findAll();
    }

    /**
     * Retrieves a reservation with the specified ID.
     *
     * @param id the ID of the reservation to retrieve
     * @return a Mono containing the reservation with the specified ID
     */
    public Mono<Reservation> getById(final Long id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")));
    }

    /**
     * Creates a new reservation and returns the ID of the created reservation.
     *
     * @param reservation the reservation object containing the details of the reservation
     * @return a Mono containing the ID of the created reservation
     */
    public Mono<Long> createReservation(final Reservation reservation) {
        return capacityRepository.findByAmenityType(reservation.getAmenityType())
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

    /**
     * Updates the reservation with the given ID.
     *
     * @param id          the ID of the reservation to update
     * @param reservation the updated reservation object
     * @return a Mono<Void> indicating completion
     */
    public Mono<Void> updateReservation(final Long id, final Reservation reservation) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(existingReservation -> {
                    reservation.setId(id);
                    return reservationRepository.save(reservation);
                })
                .then();
    }

    /**
     * Deletes a record from the reservation repository based on the given ID.
     *
     * @param id the ID of the record to be deleted
     * @return a Mono<Void> indicating completion
     */
    public Mono<Void> deleteReservation(final Long id) {
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(reservationRepository::delete);
    }

    /**
     * Retrieves a reservation based on the reservation name.
     *
     * @param reservationName the name of the reservation to retrieve
     * @return a Mono containing the reservation with the specified name
     */
    @Override
    public Mono<Reservation> getReservationByReservationName(String reservationName) {
        return reservationRepository.findReservationByReservationName(reservationName);
    }
}