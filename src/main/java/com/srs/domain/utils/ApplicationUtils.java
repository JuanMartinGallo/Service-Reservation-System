package com.srs.domain.utils;

import com.srs.domain.models.Role;
import com.srs.domain.models.User;
import com.srs.domain.models.dto.RegisterRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class ApplicationUtils {

    private static final PasswordEncoder passwordEncoder = new PasswordEncoder() {
        @Override
        public String encode(CharSequence rawPassword) {
            return "";
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return false;
        }
    };

    private ApplicationUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static User mapToEntity(final RegisterRequest request) {
        return User
                .builder()
                .fullname(request.getFullname())
                .username(request.getUsername())
                .country(request.getCountry())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(String.valueOf(Role.USER))
                .build();
    }

}