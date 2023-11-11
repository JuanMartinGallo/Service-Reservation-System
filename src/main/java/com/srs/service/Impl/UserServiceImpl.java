package com.srs.service.Impl;

import com.srs.model.DTO.RegisterRequest;
import com.srs.model.Role;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import com.srs.service.UserService;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  @Override
  public User get(final Long id) {
    return userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  @Override
  public Long create(final User user) {
    return userRepository.save(user).getId();
  }

  @Override
  public void update(final Long id, final User user) {
    userRepository
        .findById(id)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    userRepository.save(user);
  }

  @Override
  public void delete(final Long id) {
    userRepository.deleteById(id);
  }

  @Override
  public User getUserByUsername(String username) {
    return userRepository.findUserByUsername(username);
  }

  public User mapToEntity(@Valid final RegisterRequest request) {
    User user = User
      .builder()
      .fullname(request.getFullname())
      .username(request.getUsername())
      .country(request.getCountry())
      .password(passwordEncoder.encode(request.getPassword()))
      .role(Role.USER)
      .build();

    return user;
  }
  // public RegisteredRequest mapToDTO(
  //   final User user,
  //   final RegisteredRequest request
  // ) {
  //   request.setFirstname(user.getFirstName());
  //   request.setLastname(user.getLastName());
  //   request.setEmail(user.getEmail());
  //   request.setPassword(user.getPassword());
  //   request.setRole(getAuthorities(Roles.USER));
  //   return request;
  // }
}
