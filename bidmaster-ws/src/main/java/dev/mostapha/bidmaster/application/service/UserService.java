package dev.mostapha.bidmaster.application.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Implementación transaccional del caso de uso de usuarios con hashing BCrypt.
 */
@Service
@Transactional(readOnly = true)
public class UserService implements UserUseCase {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public User registerUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("El nombre de usuario ya está en uso");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("El correo electrónico ya está en uso");
        }

        // Cifrar la contraseña con BCrypt si aún no está cifrada
        if (user.getPassword() != null && !user.getPassword().startsWith("$2")) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public List<User> findUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    @Transactional
    public User updateUser(User updatedUser) {
        User existingUser = getUserOrThrow(updatedUser.getId());

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setPhone(updatedUser.getPhone());
        existingUser.setAddress(updatedUser.getAddress());
        existingUser.setRole(updatedUser.getRole());

        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void updateUserStatus(UUID id, UserStatus status) {
        User user = getUserOrThrow(id);
        switch (status) {
            case ACTIVE -> user.unblock();
            case BLOCKED -> user.block();
            case SUSPENDED -> user.suspend();
            case BANNED -> user.ban();
        }
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public boolean recordSuccessfulLogin(UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.recordSuccessfulLogin();
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean recordFailedLogin(UUID id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.incrementFailedLogins();
                    userRepository.save(user);
                    return true;
                })
                .orElse(false);
    }

    @Override
    @Transactional
    public boolean addBalance(UUID id, BigDecimal amount) {
        User user = getUserOrThrow(id);
        user.addBalance(amount);
        userRepository.save(user);
        return true;
    }

    @Override
    @Transactional
    public boolean subtractBalance(UUID id, BigDecimal amount) {
        User user = getUserOrThrow(id);
        boolean success = user.subtractBalance(amount);
        if (success) {
            userRepository.save(user);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean blockBalance(UUID id, BigDecimal amount) {
        User user = getUserOrThrow(id);
        boolean success = user.blockBalance(amount);
        if (success) {
            userRepository.save(user);
        }
        return success;
    }

    @Override
    @Transactional
    public boolean unblockBalance(UUID id, BigDecimal amount) {
        User user = getUserOrThrow(id);
        boolean success = user.unblockBalance(amount);
        if (success) {
            userRepository.save(user);
        }
        return success;
    }

    @Override
    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public User login(String usernameOrEmail, String password) {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findByEmail(usernameOrEmail))
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new IllegalArgumentException("La cuenta de usuario no está activa (" + user.getStatus() + ")");
        }

        boolean passwordMatches = matchesPassword(password, user.getPassword());
        if (passwordMatches) {
            user.recordSuccessfulLogin();
            return userRepository.save(user);
        } else {
            user.incrementFailedLogins();
            userRepository.save(user);
            throw new IllegalArgumentException("Credenciales inválidas");
        }
    }

    private boolean matchesPassword(String rawPassword, String storedPassword) {
        if (rawPassword == null || storedPassword == null) {
            return false;
        }
        if (storedPassword.startsWith("$2")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return storedPassword.equals(rawPassword);
    }

    private User getUserOrThrow(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario no encontrado"));
    }
}
