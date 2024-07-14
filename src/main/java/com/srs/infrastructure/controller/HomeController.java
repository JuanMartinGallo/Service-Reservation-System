package com.srs.infrastructure.controller;

import com.github.dockerjava.api.exception.UnauthorizedException;
import com.srs.domain.models.Reservation;
import com.srs.domain.models.ReservationMapper;
import com.srs.domain.models.dto.ReservationDTO;
import com.srs.domain.services.ReservationService;
import com.srs.domain.services.impl.UserServiceImpl;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.Set;

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
            @ModelAttribute ReservationDTO reservationDTO,
            BindingResult bindingResult,
            Authentication authentication,
            Model model
    ) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails userDetails) {
            return userService.getUserByUsername(userDetails.getUsername())
                    .flatMap(reservationUser -> {
                        reservationDTO.setUsername(userDetails.getUsername());
                        reservationDTO.setUserId(reservationUser.getId());
                        log.debug("Reservation DTO: {}", reservationDTO);

                        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                        Validator validator = factory.getValidator();
                        Set<ConstraintViolation<ReservationDTO>> violations = validator.validate(reservationDTO);

                        if (!violations.isEmpty()) {
                            for (ConstraintViolation<ReservationDTO> violation : violations) {
                                bindingResult.addError(new FieldError("reservationDTO", violation.getPropertyPath().toString(), violation.getMessage()));
                            }
                            model.addAttribute("message", bindingResult.getAllErrors());
                            return Mono.just("errors/error");
                        }

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
                                    model.addAttribute("message", e.getMessage());
                                    return Mono.just("errors/error");
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Error in reservationsSubmit: ", e);
                        model.addAttribute("message", e.getMessage());
                        return Mono.just("errors/error");
                    });
        } else {
            log.error("Principal is not an instance of UserDetails");
            model.addAttribute("message", "Authentication error");
            return Mono.just("errors/error");
        }
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