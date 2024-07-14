package com.srs.domain.services.impl;

import com.srs.domain.exceptions.CapacityFullException;
import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Reservation;
import com.srs.domain.models.ReservationMapper;
import com.srs.domain.models.dto.ReservationDTO;
import com.srs.domain.repositories.CapacityRepository;
import com.srs.domain.repositories.ReservationRepository;
import com.srs.domain.services.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReservationServiceImpl implements ReservationService {

    private final ReservationRepository reservationRepository;
    private final CapacityRepository capacityRepository;
    private final ReservationMapper reservationMapper;

    @Override
    public Flux<ReservationDTO> findAll() {
        return reservationRepository.findAll()
                .doOnNext(reservation -> log.debug("Found reservation: {}", reservation))
                .map(reservationMapper::toDto)
                .doOnNext(reservationDTO -> log.debug("Mapped to DTO: {}", reservationDTO));
    }

    @Override
    public Mono<ReservationDTO> getById(final Long id) {
        return reservationRepository.findById(id)
                .doOnNext(reservation -> log.debug("Found reservation by ID {}: {}", id, reservation))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .map(reservationMapper::toDto)
                .doOnNext(reservationDTO -> log.debug("Mapped to DTO: {}", reservationDTO));
    }

    @Override
    public Mono<Long> createReservation(final ReservationDTO reservationDTO) {
        log.debug("Creating reservation from DTO: {}", reservationDTO);
        return capacityRepository.findByAmenityType(AmenityType.valueOf(reservationDTO.getAmenityType()))
                .flatMap(capacity -> reservationRepository
                        .findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
                                LocalDate.parse(reservationDTO.getReservationDate()),
                                LocalTime.parse(reservationDTO.getStartTime()),
                                LocalTime.parse(reservationDTO.getEndTime()),
                                LocalTime.parse(reservationDTO.getStartTime()),
                                LocalTime.parse(reservationDTO.getEndTime())
                        )
                        .count()
                        .flatMap(overlappingReservations -> {
                            if (overlappingReservations >= capacity.getCapacity()) {
                                return Mono.error(new CapacityFullException(
                                        "This amenity's capacity is full at desired time"));
                            } else {
                                Reservation reservation = reservationMapper.toEntity(reservationDTO);
                                log.debug("Mapped to entity: {}", reservation);
                                return reservationRepository.save(reservation)
                                        .doOnNext(savedReservation -> log.debug("Saved reservation: {}", savedReservation))
                                        .map(Reservation::getId);
                            }
                        })
                );
    }

    @Override
    public Mono<Void> updateReservation(final Long id, final ReservationDTO reservationDTO) {
        log.debug("Updating reservation ID {} with DTO: {}", id, reservationDTO);
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(existingReservation -> {
                    Reservation reservation = reservationMapper.toEntity(reservationDTO);
                    reservation.setId(id);
                    log.debug("Mapped to entity for update: {}", reservation);
                    return reservationRepository.save(reservation);
                })
                .doOnSuccess(unused -> log.debug("Updated reservation ID {}", id))
                .then();
    }

    @Override
    public Mono<Void> deleteReservation(final Long id) {
        log.debug("Deleting reservation ID {}", id);
        return reservationRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Reservation not found")))
                .flatMap(reservationRepository::delete)
                .doOnSuccess(unused -> log.debug("Deleted reservation ID {}", id))
                .then();
    }

    @Override
    public Mono<ReservationDTO> getReservationByReservationDate(String reservationDate) {
        log.debug("Getting reservation by date: {}", reservationDate);
        return reservationRepository.findReservationByReservationDate(reservationDate)
                .map(reservationMapper::toDto)
                .doOnNext(reservationDTO -> log.debug("Mapped to DTO: {}", reservationDTO));
    }
}