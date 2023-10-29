package com.srs.tests;

import static org.junit.jupiter.api.Assertions.*;

import com.srs.model.User;
import com.srs.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@RequiredArgsConstructor
public class UserServiceTests {

  private final UserRepository userRepository;

  @Test
  @DisplayName("Must Find all users")
  public void testFindAll() {
    List<User> actualUsers = userRepository.findAll();

    assertNotNull(actualUsers);
  }
}
