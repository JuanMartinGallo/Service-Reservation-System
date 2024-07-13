package com.srs.infrastructure.controller;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.srs.domain.models.Reservation;
import com.srs.domain.models.ReservationMapper;
import com.srs.domain.models.User;
import com.srs.domain.models.dto.ReservationDTO;
import com.srs.domain.services.ReservationService;
import com.srs.domain.services.impl.UserServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
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
    private final ReservationMapper reservationMapper;

    @GetMapping({"/", "/index", "/home"})
    public Mono<String> home(Authentication authentication, Model model, WebSession session, ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().toString();
        log.debug("GET {} called", path);

        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("User is already authenticated in home");
            Object principal = authentication.getPrincipal();
            if (principal instanceof UserDetails userDetails) {
                return userService.getUserByUsername(userDetails.getUsername())
                        .flatMap(user -> {
                            log.debug("User fetched, adding to session attributes");
                            session.getAttributes().put("user", user);
                            Reservation reservation = new Reservation();
                            model.addAttribute("reservation", reservation);
                            return Mono.just("index");
                        });
            }
        } else {
            log.debug("User is not authenticated");
        }
        return Mono.just("index");
    }


    @GetMapping("/reservations")
    public Mono<String> reservations(Authentication authentication, Model model, WebSession session) {
        log.debug("GET /reservations called");
        if (authentication != null && authentication.isAuthenticated()) {
            log.debug("User is already authenticated reservation controller");
            Object principal = authentication.getPrincipal();
            log.debug("Principal: {}", principal);
            if (principal instanceof UserDetails userDetails) {
                log.debug("User details: {}", userDetails);
                return userService.getUserByUsername(userDetails.getUsername())
                        .flatMap(user -> {
                            log.debug("User fetched, adding to session attributes");
                            session.getAttributes().put("user", user);
                            ReservationDTO reservationDTO = new ReservationDTO();
                            model.addAttribute("reservationDTO", reservationDTO);
                            model.addAttribute("authenticated", true);
                            return Mono.just("reservations");
                        });
            } else {
                log.warn("Principal is not an instance of UserDetails");
            }
        }
        return Mono.error(new UnauthorizedException("User not authenticated"));
    }

    @PostMapping("/reservations-submit")
    public Mono<String> reservationsSubmit(
            @Valid @ModelAttribute ReservationDTO reservationDTO,
            BindingResult bindingResult,
            @SessionAttribute Mono<User> user
    ) {
        if (bindingResult.hasErrors()) {
            log.error("Error in reservationsSubmit: {}", bindingResult.getAllErrors());
            return Mono.just("error");
        }

        return user.flatMap(reservationUser -> {
            reservationDTO.setUsername(reservationUser.getUsername());
            reservationDTO.setUserId(reservationUser.getId());
            return reservationService.createReservation(reservationDTO)
                    .flatMap(reservationService::getById)
                    .flatMap(reservationFromDb -> {
                        Reservation reservationEntity = reservationMapper.toEntity(reservationFromDb);
                        reservationUser.getReservations().add(reservationEntity);
                        return userService.updateUser(reservationUser.getId(), reservationUser)
                                .thenReturn("reservations");
                    })
                    .onErrorResume(e -> {
                        log.error("Error in reservationsSubmit after userService.updateUser: ", e);
                        return Mono.just("error");
                    });
        }).onErrorResume(e -> {
            log.error("Error in reservationsSubmit: ", e);
            return Mono.just("error");
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