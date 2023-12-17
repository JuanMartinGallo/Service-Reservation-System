package com.srs.repository;

import com.srs.TestHelper;
import com.srs.model.User;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.EmbeddedDatabaseConnection;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(connection = EmbeddedDatabaseConnection.H2)
@Import(TestHelper.class)
public class UserRepositoryTests {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestHelper testHelper;

  @Nested
  class UsersTest {

    @Test
    @DisplayName("Must return saved user")
    public void returnSavedUser() {
      User testUser = testHelper.getTestUser();
      User userSaved = userRepository.save(testUser);
      Assertions.assertEquals(testUser, userSaved);
      Assertions.assertNotNull(userSaved);
    }

    @Test
    @DisplayName("Must return user id")
    public void returnUserId() {
      User testUser = testHelper.getTestUser();
      userRepository.save(testUser);
      User userSaved = userRepository.findById(testUser.getId()).get();
      Assertions.assertNotNull(userSaved);
    }

    @Test
    @DisplayName("Must update user")
    public void updateUser() {
      User testUser = testHelper.getTestUser();
      userRepository.save(testUser);
      User userSaved = userRepository.findById(testUser.getId()).get();
      userSaved.setFullname("test2");
      userSaved.setUsername("testuser2");
      userSaved.setCountry("testcountry2");
      userSaved.setPassword("testpassword2");

      User updatedUser = userRepository.save(userSaved);
      Assertions.assertEquals(updatedUser, userSaved);
      Assertions.assertNotNull(updatedUser);
    }

    @Test
    @DisplayName("Must return user id")
    public void deleteUser() {
      User testUser = testHelper.getTestUser();
      userRepository.save(testUser);
      userRepository.deleteById(testUser.getId());
      Optional<User> userReturned = userRepository.findById(testUser.getId());
      Assertions.assertFalse(userReturned.isPresent());
    }
  }
}
