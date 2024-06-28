package com.srs.application.runners;

import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Capacity;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.CapacityRepository;
import com.srs.domain.repositories.ReservationRepository;
import com.srs.domain.repositories.UserRepository;
import com.srs.domain.services.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class DataLoader {

    private final AuthService authService;
    private final Map<AmenityType, Integer> initialCapacities = createInitialCapacities();

    private static Map<AmenityType, Integer> createInitialCapacities() {
        Map<AmenityType, Integer> capacities = new HashMap<>();
        capacities.put(AmenityType.GYM, 20);
        capacities.put(AmenityType.POOL, 4);
        capacities.put(AmenityType.SAUNA, 1);
        return capacities;
    }

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository,
                                      ReservationRepository reservationRepository,
                                      CapacityRepository capacityRepository) {
        return args -> {
            RegisterRequest defaultUser = RegisterRequest.builder()
                    .fullname("Juan Martin Gallo")
                    .username("juanmartin")
                    .country("Argentina")
                    .password("12345")
                    .build();

            authService.register(defaultUser);

            for (AmenityType amenityType : initialCapacities.keySet()) {
                capacityRepository.save(new Capacity(amenityType, initialCapacities.get(amenityType)));
            }
        };
    }
}
