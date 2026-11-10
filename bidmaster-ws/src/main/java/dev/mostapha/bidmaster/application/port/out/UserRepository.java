package dev.mostapha.bidmaster.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Puerto de salida para la persistencia de usuarios.
 */
public interface UserRepository {

    User save(User user);

    User updateUser(User user);

    Optional<User> findById(UUID id);

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findAll();

    List<User> findByStatus(UserStatus status);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void deleteById(UUID id);
}
