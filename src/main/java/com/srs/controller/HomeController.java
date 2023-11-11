package com.srs.controller;

import com.srs.model.Reservation;
import com.srs.model.User;
import com.srs.service.Impl.UserServiceImpl;
import com.srs.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

@Controller
@AllArgsConstructor
public class HomeController {

  private final UserServiceImpl userService;
  private final ReservationService reservationService;

  @GetMapping({"/", "/index", "/home"})
  public String home(Model model) {
    return "index";
  }

  @GetMapping("/reservations")
  public String reservations(Model model, HttpSession session) {
    UserDetails principal = (UserDetails) SecurityContextHolder
      .getContext()
      .getAuthentication()
      .getPrincipal();
    String name = principal.getUsername();
    User user = userService.getUserByUsername(name);

    // This should always be the case
    if (user != null) {
      session.setAttribute("user", user);

      // Empty reservation object in case the user creates a new reservation
      Reservation reservation = new Reservation();
      model.addAttribute("reservation", reservation);

      return "reservations";
    }

    return "index";
  }

  @PostMapping("/reservations-submit")
  public String reservationsSubmit(
    @ModelAttribute Reservation reservation,
    @SessionAttribute("user") User user
  ) {
    // Save to DB after updating
    assert user != null;
    reservation.setUser(user);
    reservationService.create(reservation);
    Set<Reservation> userReservations = user.getReservations();
    userReservations.add(reservation);
    user.setReservations(userReservations);
    userService.update(user.getId(), user);
    return "reservations";
  }
}
