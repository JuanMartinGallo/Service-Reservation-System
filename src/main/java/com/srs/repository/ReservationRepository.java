package com.srs.repository;

import com.srs.model.AmenityType;
import com.srs.model.Reservation;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository
  extends JpaRepository<Reservation, Long> {
  List<Reservation> findReservationsByAmenityType(AmenityType amenityType);

  List<Reservation> findReservationsByReservationDateAndStartTimeBeforeAndEndTimeAfterOrStartTimeBetween(
    LocalDate reservationDate,
    LocalTime startTime,
    LocalTime endTime,
    LocalTime betweenStart,
    LocalTime betweenEnd
  );
}
