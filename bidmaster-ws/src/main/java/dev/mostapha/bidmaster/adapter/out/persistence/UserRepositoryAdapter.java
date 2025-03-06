package dev.mostapha.bidmaster.adapter.out.persistence;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.domain.model.user.Address;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import dev.mostapha.bidmaster.infrastructure.persistence.entities.UserEntity;
import dev.mostapha.bidmaster.infrastructure.persistence.repositories.InfraUserRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Adaptador de salida que implementa el puerto de repositorio de usuarios.
 * Conecta el dominio con la infraestructura de persistencia.
 */
@Component
public class UserRepositoryAdapter implements UserRepository {

    private final InfraUserRepository userRepository;
    private final ObjectMapper objectMapper;

    public UserRepositoryAdapter(InfraUserRepository userRepository, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<User> save(User user) {
        return Mono.just(user)
                .map(this::mapToEntity)
                .flatMap(userRepository::save)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<User> findById(UUID id) {
        return userRepository.findById(id)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<User> findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::mapToDomain);
    }

    @Override
    public Mono<User> findByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(this::mapToDomain);
    }

    @Override
    public Flux<User> findAll() {
        return userRepository.findAll()
                .map(this::mapToDomain);
    }

    @Override
    public Flux<User> findByStatus(UserStatus status) {
        return userRepository.findByStatus(status.name())
                .map(this::mapToDomain);
    }

    @Override
    public Mono<Boolean> existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public Mono<Boolean> existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return userRepository.deleteById(id);
    }

    @Override
    public Mono<Void> updateStatus(UUID id, UserStatus status) {
        return userRepository.updateUserStatus(id, status.name());
    }

    @Override
    public Mono<Void> incrementFailedLoginAttempts(UUID id) {
        return userRepository.incrementFailedLoginAttempts(id);
    }

    @Override
    public Mono<Void> resetFailedLoginAttempts(UUID id) {
        return userRepository.resetFailedLoginAttempts(id);
    }

    /**
     * Mapea una entidad de dominio a una entidad de persistencia
     */
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

    /**
     * Mapea una entidad de persistencia a una entidad de dominio
     */
    private User mapToDomain(UserEntity entity) {
        Address address = null;
        if (entity.getAddress() != null) {
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
