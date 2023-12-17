package com.srs;

import com.srs.model.DTO.RegisterRequest;
import com.srs.model.Role;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import io.jsonwebtoken.JwtException;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@AllArgsConstructor
public class TestHelper {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  public User getTestUser() {
    return User
      .builder()
      .fullname("Test User")
      .username("testuser")
      .country("Test Country")
      .password(passwordEncoder.encode("testpassword"))
      .role(Role.USER)
      .build();
  }

  public User getSpecificUser(Long id) {
    Optional<User> user = userRepository.findById(id);
    if (user.isPresent()) {
      return user.get();
    } else {
      throw new JwtException("User not found");
    }
  }

  public RegisterRequest mapToDTO(final User user) {
    RegisterRequest request = new RegisterRequest();
    request.setFullname(user.getFullname());
    request.setUsername(user.getUsername());
    request.setCountry(user.getCountry());
    request.setPassword(user.getPassword());
    return request;
  }
}
