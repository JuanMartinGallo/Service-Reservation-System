package com.srs.domain.utils;

import com.srs.domain.models.Roles;
import com.srs.domain.models.User;
import com.srs.domain.models.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class ApplicationUtils {

    private ApplicationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static User mapToEntity(final RegisterRequest request, PasswordEncoder passwordEncoder) {
        return User.builder()
                .fullname(request.getFullname())
                .username(request.getUsername())
                .country(request.getCountry())
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(String.valueOf(Roles.ROLE_USER))
                .build();
    }
}
