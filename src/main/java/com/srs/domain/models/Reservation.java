package com.srs.domain.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("reservations")
public class Reservation {

    @Id
    @Column("id")
    private Long id;

    @Column("username")
    private String username;

    @DateTimeFormat(pattern = "MM/dd/yyyy")
    @Column("reservation_date")
    private LocalDate reservationDate;

    @DateTimeFormat(pattern = "HH:mm")
    @Column("start_time")
    private LocalTime startTime;

    @DateTimeFormat(pattern = "HH:mm")
    @Column("end_time")
    private LocalTime endTime;

    @Column("amenity_type")
    private AmenityType amenityType;

    @Column("user_id")
    private Long userId;
}