package com.srs;

import com.srs.model.AmenityType;
import com.srs.model.Capacity;
import com.srs.model.User;
import com.srs.repository.CapacityRepository;
import com.srs.repository.ReservationRepository;
import com.srs.repository.UserRepository;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootApplication
public class ServiceReservationSystemApplication {

  private Map<AmenityType, Integer> initialCapacities = new HashMap<>() {
    {
      put(AmenityType.GYM, 20);
      put(AmenityType.POOL, 4);
      put(AmenityType.SAUNA, 1);
    }
  };

  public static void main(String[] args) {
    SpringApplication.run(ServiceReservationSystemApplication.class, args);
  }

  @Bean
  public CommandLineRunner loadData(
    UserRepository userRepository,
    ReservationRepository reservationRepository,
    CapacityRepository capacityRepository
  ) {
    return args -> {
      userRepository.save(
        new User(
          "Juan Martin Gallo",
          "juanmartin",
          bCryptPasswordEncoder().encode("12345")
        )
      );
      for (AmenityType amenityType : initialCapacities.keySet()) {
        capacityRepository.save(
          new Capacity(amenityType, initialCapacities.get(amenityType))
        );
      }
    };
  }

  @Bean
  public BCryptPasswordEncoder bCryptPasswordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
