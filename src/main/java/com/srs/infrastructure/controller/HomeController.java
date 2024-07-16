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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static com.srs.domain.utils.ApplicationConstants.*;

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
                            log.debug("User fetched: {}", user);
                            log.debug("User reservations: {}", user.getReservations());
                            session.getAttributes().put("user", user);
                            log.debug("Session user: {}", Optional.ofNullable(session.getAttribute("user")));
                            ReservationDTO reservationDTO = new ReservationDTO();
                            model.addAttribute("reservationDTO", reservationDTO);
                            model.addAttribute("authenticated", true);
                            model.addAttribute("reservations", user.getReservations() != null ? user.getReservations() : Collections.emptySet());
                            user.getReservations().forEach(reservation -> log.debug("Reservation ID: {}", reservation.getId()));
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
                        reservationDTO.setUsername(reservationUser.getUsername());
                        reservationDTO.setUserId(reservationUser.getId());
                        log.debug("Reservation DTO: {}", reservationDTO);

                        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
                        Validator validator = factory.getValidator();
                        Set<ConstraintViolation<ReservationDTO>> violations = validator.validate(reservationDTO);

                        if (!violations.isEmpty()) {
                            for (ConstraintViolation<ReservationDTO> violation : violations) {
                                bindingResult.addError(new FieldError(RESERVATION_DTO, violation.getPropertyPath().toString(), violation.getMessage()));
                            }
                            model.addAttribute(MESSAGE, bindingResult.getAllErrors());
                            return Mono.just(ERROR_PATH);
                        }

                        return reservationService.createReservation(reservationDTO)
                                .flatMap(reservationService::getById)
                                .flatMap(reservationFromDb -> {
                                    Reservation reservationEntity = reservationMapper.toEntity(reservationFromDb);
                                    reservationUser.getReservations().add(reservationEntity);
                                    return userService.updateUser(reservationUser.getId(), reservationUser)
                                            .thenReturn(reservationUser);
                                })
                                .flatMap(updatedUser -> {
                                    model.addAttribute(RESERVATION_DTO, new ReservationDTO());
                                    model.addAttribute("authenticated", true);
                                    model.addAttribute("reservations", updatedUser.getReservations() != null ? updatedUser.getReservations() : Collections.emptySet());
                                    return Mono.just("reservations");
                                })
                                .onErrorResume(e -> {
                                    log.error("Error in reservationsSubmit after userService.updateUser: ", e);
                                    model.addAttribute(MESSAGE, e.getMessage());
                                    return Mono.just(ERROR_PATH);
                                });
                    })
                    .onErrorResume(e -> {
                        log.error("Error in reservationsSubmit: ", e);
                        model.addAttribute(MESSAGE, e.getMessage());
                        return Mono.just(ERROR_PATH);
                    });
        } else {
            log.error("Principal is not an instance of UserDetails");
            model.addAttribute(MESSAGE, "Authentication error");
            return Mono.just(ERROR_PATH);
        }
    }

    @PostMapping("/delete-reservation")
    public Mono<String> deleteReservation(
            @RequestParam Long id,
            ServerWebExchange exchange,
            BindingResult bindingResult,
            Model model
    ) {
        log.debug("POST /delete-reservation called with id={}", id);

        exchange.getFormData().subscribe(formData -> log.debug("Form Data: {}", formData));

        if (bindingResult.hasErrors()) {
            log.error("BindingResult has errors: {}", bindingResult.getAllErrors());
            model.addAttribute(MESSAGE, "Error in request parameters");
            return Mono.just(ERROR_PATH);
        }

        return reservationService.deleteReservation(id)
                .then(Mono.just("redirect:/reservations"))
                .onErrorResume(e -> {
                    log.error("Error deleting reservation: ", e);
                    model.addAttribute("message", "Error deleting reservation: " + e.getMessage());
                    return Mono.just(ERROR_PATH);
                });
    }

}