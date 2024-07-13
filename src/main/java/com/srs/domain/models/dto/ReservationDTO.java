package com.srs.domain.models.dto;

import com.srs.domain.models.AmenityType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReservationDTO {

    @NotNull(message = "Username is required")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @NotNull(message = "Reservation date is required")
    private String reservationDate;

    @NotNull(message = "Start time is required")
    private String startTime;

    @NotNull(message = "End time is required")
    private String endTime;

    @NotNull(message = "Amenity type is required")
    private AmenityType amenityType;

    @NotNull(message = "User ID is required")
    private Long userId;
}