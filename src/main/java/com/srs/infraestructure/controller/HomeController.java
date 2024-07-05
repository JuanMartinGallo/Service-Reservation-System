package com.srs.infraestructure.controller;

import com.srs.domain.models.Reservation;
import com.srs.domain.models.User;
import com.srs.domain.services.ReservationService;
import com.srs.domain.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

@Controller
@RequiredArgsConstructor
@Slf4j
public class HomeController {

    private final UserServiceImpl userService;
    private final ReservationService reservationService;

    @GetMapping({"/", "/index", "/home"})
    public Mono<String> home() {
        log.debug("GET /home called");
        return Mono.just("index");
    }

    @GetMapping("/reservations")
    public Mono<String> reservations(ServerWebExchange exchange, Model model, WebSession session) {
        log.debug("GET /reservations called");
        return exchange.getPrincipal()
                .cast(UserDetails.class)
                .flatMap(principal -> {
                    log.debug("Fetching user with username {}", principal.getUsername());
                    return userService.getUserByUsername(principal.getUsername());
                })
                .flatMap(user -> {
                    log.debug("User fetched, adding to session attributes");
                    session.getAttributes().put("user", user);
                    Reservation reservation = new Reservation();
                    model.addAttribute("reservation", reservation);
                    return Mono.just("reservations");
                });
    }

    @PostMapping("/reservations-submit")
    public Mono<String> reservationsSubmit(
            @ModelAttribute Reservation reservation,
            @SessionAttribute Mono<User> user,
            Model model
    ) {
        log.debug("POST /reservations-submit called with reservation={}", reservation);
        return user.flatMap(reservationUser -> {
            log.debug("Setting username in reservation");
            reservation.setUsername(reservationUser.getUsername());
            log.debug("Creating reservation");
            return reservationService.createReservation(reservation)
                    .flatMap(savedIdReservation -> {
                        log.debug("Fetching reservation by id {}", savedIdReservation);
                        return reservationService.getById(savedIdReservation);
                    })
                    .flatMap(reservationFromDb -> {
                        log.debug("Adding reservation to user's reservations and updating user");
                        reservationUser.getReservations().add(reservationFromDb);
                        return userService.updateUser(reservationUser.getId(), reservationUser)
                                .thenReturn("reservations");
                    });
        });
    }

    @PostMapping("/delete-reservations")
    public Mono<String> deleteReservations(
            @ModelAttribute Reservation reservation
    ) {
        log.debug("POST /delete-reservations called with reservation={}", reservation);
        return reservationService.deleteReservation(reservation.getId())
                .thenReturn("redirect:/reservations");
    }
}

