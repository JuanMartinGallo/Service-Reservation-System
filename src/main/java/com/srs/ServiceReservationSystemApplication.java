package com.srs;

import com.srs.auth.AuthService;
import com.srs.model.AmenityType;
import com.srs.model.Capacity;
import com.srs.model.DTO.RegisterRequest;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import com.srs.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class ServiceReservationSystemApplication {

  private AuthService authService;

  public ServiceReservationSystemApplication(
    @Autowired AuthService authService
  ) {
    this.authService = authService;
  }

  private Map<AmenityType, Integer> initialCapacities = new HashMap<>() {
    {
      put(AmenityType.GYM, 20);
      put(AmenityType.POOL, 4);
      put(AmenityType.SAUNA, 1);
    }
  };

  public static void main(String[] args) {
    SpringApplication.run(ServiceReservationSystemApplication.class, args);
  }//TODO: revisar si el programa funciona bien

  @Bean
  public CommandLineRunner loadData(
    UserRepository userRepository,
    ReservationRepository reservationRepository,
    CapacityRepository capacityRepository
  ) {
    return args -> {
      RegisterRequest defaultUser = RegisterRequest
        .builder()
        .fullname("Juan Martin Gallo")
        .username("juanmartin")
        .country("Argentina")
        .password("12345")
        .build();

      authService.register(defaultUser);

      for (AmenityType amenityType : initialCapacities.keySet()) {
        capacityRepository.save(
          new Capacity(amenityType, initialCapacities.get(amenityType))
        );
      }
    };
  }
}
