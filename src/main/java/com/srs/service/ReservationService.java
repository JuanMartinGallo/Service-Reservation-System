package com.srs.service;

import com.srs.exceptions.CapacityFullException;
import com.srs.model.Reservation;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
@AllArgsConstructor
public class ReservationService {

  private final ReservationRepository reservationRepository;
  private final CapacityRepository capacityRepository;

  /**
   * Retrieves all reservations.
   *
   * @return a list of Reservation objects representing all reservations
   */
  public List<Reservation> findAll() {
    return reservationRepository.findAll();
  }

  /**
   * Retrieves a reservation with the specified ID.
   *
   * @param  id  the ID of the reservation to retrieve
   * @return     the reservation with the specified ID
   */
  public Reservation get(final Long id) {
    return reservationRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  /**
   * Creates a new reservation and returns the ID of the created reservation.
   *
   * @param  reservation  the reservation object containing the details of the reservation
   * @return              the ID of the created reservation
   */
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

  /**
   * Updates the reservation with the given ID.
   *
   * @param  id          the ID of the reservation to update
   * @param  reservation the updated reservation object
   */
  public void update(final Long id, final Reservation reservation) {
    reservationRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    reservationRepository.save(reservation);
  }

  /**
   * Deletes a record from the reservation repository based on the given ID.
   *
   * @param  id  the ID of the record to be deleted
   */
  public void delete(final Long id) {
    reservationRepository.deleteById(id);
  }
}
