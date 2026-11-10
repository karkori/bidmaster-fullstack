package dev.mostapha.bidmaster.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mostapha.bidmaster.adapter.out.persistence.entity.UserEntity;
import dev.mostapha.bidmaster.adapter.out.persistence.repository.JpaUserRepository;
import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.domain.model.user.Address;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Adaptador de salida JPA para el repositorio de usuarios.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final JpaUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserRepositoryAdapter(JpaUserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public User save(User user) {
        UserEntity entity = mapToEntity(user);
        if (!userRepository.existsById(entity.getId())) {
            entity.setVersion(null);
        }
        UserEntity saved = userRepository.save(entity);
        return mapToDomain(saved);
    }

    @Override
    public User updateUser(User user) {
        return save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return userRepository.findById(id).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::mapToDomain);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email).map(this::mapToDomain);
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll().stream().map(this::mapToDomain).toList();
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status.name()).stream().map(this::mapToDomain).toList();
    }

    @Override
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public void deleteById(UUID id) {
        userRepository.deleteById(id);
    }

    private UserEntity mapToEntity(User user) {
        try {
            String addressJson = user.getAddress() != null
                    ? objectMapper.writeValueAsString(user.getAddress())
                    : null;

            return UserEntity.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .password(user.getPassword())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .phone(user.getPhone())
                    .address(addressJson)
                    .status(user.getStatus().name())
                    .role(user.getRole().name())
                    .lastLogin(user.getLastLogin())
                    .failedLoginAttempts(user.getFailedLoginAttempts())
                    .balance(user.getBalance())
                    .blockedBalance(user.getBlockedBalance())
                    .reputation(user.getReputation())
                    .createdAt(user.getCreatedAt())
                    .updatedAt(user.getUpdatedAt())
                    .version(user.getVersion())
                    .build();
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error al serializar la dirección", e);
        }
    }

    private User mapToDomain(UserEntity entity) {
        Address address = null;
        if (entity.getAddress() != null && !entity.getAddress().isBlank()) {
            try {
                address = objectMapper.readValue(entity.getAddress(), Address.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Error al deserializar la dirección", e);
            }
        }

        return User.reconstitute(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getPassword(),
                entity.getFirstName(),
                entity.getLastName(),
                entity.getPhone(),
                address,
                UserStatus.valueOf(entity.getStatus()),
                UserRole.valueOf(entity.getRole()),
                entity.getLastLogin(),
                entity.getFailedLoginAttempts(),
                entity.getBalance() != null ? entity.getBalance() : BigDecimal.ZERO,
                entity.getBlockedBalance() != null ? entity.getBlockedBalance() : BigDecimal.ZERO,
                entity.getReputation() != null ? entity.getReputation() : 50,
                entity.getCreatedAt(),
                entity.getUpdatedAt(),
                entity.getVersion() != null ? entity.getVersion() : 0L);
    }
}