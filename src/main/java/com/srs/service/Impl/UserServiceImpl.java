package com.srs.service.impl;

import com.srs.model.DTO.RegisterRequest;
import com.srs.model.Role;
import com.srs.model.User;
import com.srs.repository.UserRepository;
import com.srs.service.UserService;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class UserServiceImpl implements UserService {

  private UserRepository userRepository;
  private PasswordEncoder passwordEncoder;

  /**
   * Retrieves a list of all users.
   *
   * @return  a list of User objects representing all users
   */
  @Override
  public List<User> findAll() {
    return userRepository.findAll();
  }

  /**
   * Retrieves a User object based on the given ID.
   *
   * @param  id  the ID of the User to retrieve
   * @return     the User object with the given ID
   */
  @Override
  public User get(final Long id) {
    return userRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
  }

  /**
   * Creates a new user and saves it to the repository.
   *
   * @param  user  the user object to be created
   * @return       the ID of the newly created user
   */
  @Override
  public Long create(final User user) {
    return userRepository.save(user).getId();
  }

  /**
   * Updates a user with the given ID.
   *
   * @param  id   the ID of the user to be updated
   * @param  user the updated user information
   */
  @Override
  public void update(final Long id, final User user) {
    userRepository
      .findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

    userRepository.save(user);
  }

  /**
   * Deletes a record with the specified ID.
   *
   * @param  id	the ID of the record to delete
   * @return    	void
   */
  @Override
  public void delete(final Long id) {
    userRepository.deleteById(id);
  }

  /**
   * Retrieves a user by their username.
   *
   * @param  username  the username of the user
   * @return           the user with the specified username, or null if not found
   */
  @Override
  public User getUserByUsername(String username) {
    return userRepository.findUserByUsername(username);
  }

  public User mapToEntity(final RegisterRequest request) {
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
}
