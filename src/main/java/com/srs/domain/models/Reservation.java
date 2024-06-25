package com.srs.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("reservations")
public class Reservation {

    @Id
    @Column("id")
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column("reservation_date")
    private LocalDate reservationDate;

    @DateTimeFormat(pattern = "HH:mm")
    @Column("start_time")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    @Column("end_time")
    private LocalTime endTime;

    @Column("amenity_type")
    private String amenityType;

    @Column("user_id")
    private Long userId;
}

