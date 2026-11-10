package dev.mostapha.bidmaster.application.port.in;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Puerto de entrada para las operaciones de negocio sobre usuarios.
 */
public interface UserUseCase {

    User registerUser(User user);

    Optional<User> findUserById(UUID id);

    Optional<User> findUserByUsername(String username);

    Optional<User> findUserByEmail(String email);

    List<User> findAllUsers();

    List<User> findUsersByStatus(UserStatus status);

    User updateUser(User user);

    void updateUserStatus(UUID id, UserStatus status);

    void deleteUser(UUID id);

    boolean recordSuccessfulLogin(UUID id);

    boolean recordFailedLogin(UUID id);

    boolean addBalance(UUID id, BigDecimal amount);

    boolean subtractBalance(UUID id, BigDecimal amount);

    boolean blockBalance(UUID id, BigDecimal amount);

    boolean unblockBalance(UUID id, BigDecimal amount);

    User login(String usernameOrEmail, String password);
}
