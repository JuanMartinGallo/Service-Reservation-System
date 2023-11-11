package com.srs.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.srs.model.User;
import com.srs.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

@RequiredArgsConstructor
public class UserServiceTests {

  private final UserRepository userRepository;

  // @BeforeEach
  // public void setup() {
  //   User user = new User();
  // }

  @Nested
  class UsersTest {

    @Test
    @DisplayName("Must Find all users")
    @DisabledOnOs(value = OS.MAC, disabledReason = "Not supported on Mac")
    public void testFindAll() {
      assumeTrue(System.getProperty("os.name").contains("Windows"));
      List<User> actualUsers = userRepository.findAll();

      assertNotNull(actualUsers);
    }
  }

  // @Nested
  // class StudentsTest {

  //   @Test
  //   @DisplayName("Must Find all students")
  //   public void testFindAll() {
  //     List<User> actualUsers = userRepository.findAll();
  //     assertNotNull(actualUsers);
  //   }
  // }
}
