package com.srs.service;

import com.srs.model.User;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public interface UserService {
  List<User> findAll();

  User get(final Long id);

  Long create(final User user);

  void update(final Long id, final User user);

  void delete(final Long id);

  User getUserByUsername(String username);
}
