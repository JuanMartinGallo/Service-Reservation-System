package com.srs.infraestructure.controller;

import com.srs.domain.models.Reservation;
import com.srs.domain.models.User;
import com.srs.domain.services.ReservationService;
import com.srs.domain.services.impl.UserServiceImpl;
import lombok.RequiredArgsConstructor;
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
public class HomeController {

    private final UserServiceImpl userService;
    private final ReservationService reservationService;

    @GetMapping({"/", "/index", "/home"})
    public Mono<String> home() {
        return Mono.just("index");
    }

    @GetMapping("/reservations")
    public Mono<String> reservations(ServerWebExchange exchange, Model model, WebSession session) {
        return exchange.getPrincipal()
                .cast(UserDetails.class)
                .flatMap(principal -> userService.getUserByUsername(principal.getUsername()))
                .flatMap(user -> {
                    session.getAttributes().put("user", user);
                    Reservation reservation = new Reservation();
                    model.addAttribute("reservation", reservation);
                    return Mono.just("reservations");
                })
                .switchIfEmpty(Mono.just("index"));
    }

    @PostMapping("/reservations-submit")
    public Mono<String> reservationsSubmit(
            @ModelAttribute Reservation reservation,
            @SessionAttribute Mono<User> user,
            Model model
    ) {
        return user.flatMap(reservationUser -> {
            reservation.setUsername(reservationUser.getUsername());
            return reservationService.createReservation(reservation)
                    .flatMap(savedIdReservation -> reservationService.getById(savedIdReservation)
                            .flatMap(reservationFromDb -> {
                                reservationUser.getReservations().add(reservationFromDb);
                                return userService.updateUser(reservationUser.getId(), reservationUser)
                                        .thenReturn("reservations");
                            }));
        });
    }

    @PostMapping("/delete-reservations")
    public Mono<String> deleteReservations(
            @ModelAttribute Reservation reservation
    ) {
        return reservationService.deleteReservation(reservation.getId())
                .thenReturn("redirect:/reservations");
    }
}

