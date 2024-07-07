package com.srs.application.runners;

import com.srs.domain.models.AmenityType;
import com.srs.domain.models.Capacity;
import com.srs.domain.models.dto.RegisterRequest;
import com.srs.domain.repositories.CapacityRepository;
import com.srs.domain.repositories.ReservationRepository;
import com.srs.domain.repositories.UserRepository;
import com.srs.domain.services.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.EnumMap;
import java.util.Map;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class DataLoader {

    private final AuthService authService;
    private final Map<AmenityType, Integer> initialCapacities = createInitialCapacities();

    private static Map<AmenityType, Integer> createInitialCapacities() {
        Map<AmenityType, Integer> capacities = new EnumMap<>(AmenityType.class);
        capacities.put(AmenityType.GYM, 20);
        capacities.put(AmenityType.POOL, 4);
        capacities.put(AmenityType.SAUNA, 1);
        return capacities;
    }

    @Bean
    public CommandLineRunner loadData(UserRepository userRepository,
                                      ReservationRepository reservationRepository,
                                      CapacityRepository capacityRepository) {
        return args -> userRepository.existsByUsername("juanmartin")
                .flatMap(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        RegisterRequest defaultUser = RegisterRequest.builder()
                                .fullname("Juan Martin Gallo")
                                .username("juanmartin")
                                .country("Argentina")
                                .password("12345")
                                .role("ROLE_ADMIN")
                                .build();

                        return authService.register(defaultUser);
                    } else {
                        log.info("The user already exists");
                        return Mono.empty();
                    }
                })
                .thenMany(Flux.fromIterable(initialCapacities.entrySet()))
                .flatMap(entry -> {
                    AmenityType amenityType = entry.getKey();
                    Integer capacity = entry.getValue();
                    return capacityRepository.existsByAmenityType(amenityType)
                            .flatMap(exists -> {
                                if (!exists) {
                                    return capacityRepository.save(new Capacity(amenityType, capacity));
                                } else {
                                    log.info("The capacity for {} already exists", amenityType);
                                    return Mono.empty();
                                }
                            });
                })
                .doOnError(Throwable::printStackTrace)
                .subscribe();
    }
}

