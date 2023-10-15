package com.srs.service;

import com.srs.exceptions.CapacityFullException;
import com.srs.model.Reservation;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final CapacityRepository capacityRepository;

  public List<Reservation> findAll() {
    return reservationRepository.findAll();
  }

  public Reservation get(final Long id) {
    return reservationRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  public Long create(final Reservation reservation) {
    int capacity = capacityRepository
      .findByAmenityType(reservation.getAmenityType())
      .getCapacity();
    int overlappingReservations = reservationRepository
      .findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
        reservation.getReservationDate(),
        reservation.getStartTime(),
        reservation.getEndTime(),
        reservation.getStartTime(),
        reservation.getEndTime()
      )
      .size();

    if (overlappingReservations >= capacity) {
      throw new CapacityFullException(
        "This amenity's capacity is full at desired time"
      );
    }

    return reservationRepository.save(reservation).getId();
  }

  public void update(final Long id, final Reservation reservation) {
    final Reservation existingReservation = reservationRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    reservationRepository.save(reservation);
  }

  public void delete(final Long id) {
    reservationRepository.deleteById(id);
  }
}
