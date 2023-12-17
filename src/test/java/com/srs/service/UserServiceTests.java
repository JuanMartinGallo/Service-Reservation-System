package com.srs.service;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

import com.srs.TestHelper;
import com.srs.auth.AuthResponse;
import com.srs.auth.AuthService;
import com.srs.jwt.JwtService;
import com.srs.model.DTO.RegisterRequest;
import com.srs.model.Reservation;
import com.srs.model.User;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import com.srs.repository.UserRepository;
import com.srs.service.impl.UserServiceImpl;
import io.jsonwebtoken.JwtException;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
@Import(TestHelper.class)
public class UserServiceTests {

  @Nested
  class UsersTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Spy
    private JwtService jwtService;

    @Spy
    private UserServiceImpl userService;

    @InjectMocks
    private AuthService authService;

    private AutoCloseable closeable;

    private TestHelper testHelper;

    @BeforeEach
    public void setUp() {
      closeable = MockitoAnnotations.openMocks(this);
      jwtService = Mockito.spy(new JwtService(userRepository));
      userService =
        Mockito.spy(new UserServiceImpl(userRepository, passwordEncoder));
      authService =
        new AuthService(
          userRepository,
          jwtService,
          authenticationManager,
          userService
        );
      testHelper = new TestHelper(passwordEncoder, userRepository);
    }

    @AfterEach
    void closeService() throws Exception {
      closeable.close();
    }

    @Test
    @DisplayName("Must register an user")
    public void registerAnUser() {
      User user = testHelper.getTestUser();
      RegisterRequest request = testHelper.mapToDTO(user);
      Mockito.when(userService.mapToEntity(request)).thenReturn(user);
      Mockito.when(jwtService.getToken(user)).thenReturn("testtoken");
      AuthResponse response = authService.register(request);
      Assertions.assertNotNull(response, "Expected request not to be null");
      Mockito.verify(userService, Mockito.times(1)).mapToEntity(request);
      Mockito.verify(jwtService, Mockito.times(1)).getToken(user);
    }
  }

  @Nested
  class ReservationsTest {

    private AutoCloseable closeable;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private CapacityRepository capacityRepository;

    @InjectMocks
    private ReservationService reservationService;

    @BeforeEach
    public void setUp() {
      closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void closeService() throws Exception {
      closeable.close();
    }

    @Test
    @DisplayName("Must get all reservations")
    public void getAllServices() {
      List<Reservation> actualReservations = reservationService.findAll();
      Assertions.assertNotNull(
        actualReservations,
        "Expected reservations not to be null"
      );
    }
  }

  @Nested
  class TokensTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private JwtService jwtService;

    private AutoCloseable closeable;
    private PasswordEncoder passwordEncoder;
    private TestHelper testHelper;

    @BeforeEach
    public void setUp() {
      closeable = MockitoAnnotations.openMocks(this);
      passwordEncoder = new BCryptPasswordEncoder();
      testHelper = new TestHelper(passwordEncoder, userRepository);
    }

    @AfterEach
    void closeService() throws Exception {
      closeable.close();
    }

    @Test
    @DisplayName("Must get the token from the default user")
    public void getAToken() {
      User user = testHelper.getTestUser();
      UserDetails userDetails = (UserDetails) user;
      when(userRepository.existsByUsername(user.getUsername()))
        .thenReturn(true);
      String token = jwtService.getToken(userDetails);
      Assertions.assertTrue(
        token != null && !token.isEmpty(),
        "Expected token not to be null or empty"
      );
    }

    @Test
    @DisplayName("trying to get a token from an unknown user")
    public void getATokenFromAnUnknownUser() {
      when(userRepository.findById(anyLong())).thenReturn(Optional.empty());
      Assertions.assertThrows(
        JwtException.class,
        () -> {
          UserDetails userDetails = testHelper.getSpecificUser(2L);
          jwtService.getToken(userDetails);
        }
      );
    }
  }
}
