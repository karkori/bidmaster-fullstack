package dev.mostapha.bidmaster.infrastructure.persistence.repositories;

import java.util.UUID;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

import dev.mostapha.bidmaster.infrastructure.persistence.entities.UserEntity;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Repositorio reactivo para la entidad de usuario.
 * Implementa operaciones CRUD básicas y consultas específicas.
 */
public interface InfraUserRepository extends ReactiveCrudRepository<UserEntity, UUID> {
    
    /**
     * Buscar usuario por nombre de usuario
     */
    Mono<UserEntity> findByUsername(String username);
    
    /**
     * Buscar usuario por correo electrónico
     */
    Mono<UserEntity> findByEmail(String email);
    
    /**
     * Listar usuarios por estado
     */
    Flux<UserEntity> findByStatus(String status);
    
    /**
     * Verificar si existe un usuario con el mismo nombre de usuario
     */
    Mono<Boolean> existsByUsername(String username);
    
    /**
     * Verificar si existe un usuario con el mismo correo electrónico
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * Actualizar el estado de un usuario
     */
    @Query("UPDATE users SET status = :status WHERE id = :id")
    Mono<Void> updateUserStatus(UUID id, String status);
    
    /**
     * Incrementar el contador de intentos de login fallidos
     */
    @Query("UPDATE users SET failed_login_attempts = failed_login_attempts + 1 WHERE id = :id")
    Mono<Void> incrementFailedLoginAttempts(UUID id);
    
    /**
     * Resetear el contador de intentos de login fallidos
     */
    @Query("UPDATE users SET failed_login_attempts = 0 WHERE id = :id")
    Mono<Void> resetFailedLoginAttempts(UUID id);
    
    /**
     * Insertar un usuario explícitamente, independientemente de si ya tiene un ID asignado
     */
    @Modifying
    @Query("INSERT INTO users (id, username, email, password, first_name, last_name, phone, address, status, role, "+
           "last_login, failed_login_attempts, balance, blocked_balance, reputation, created_at, updated_at, version) "+
           "VALUES (:#{#user.id}, :#{#user.username}, :#{#user.email}, :#{#user.password}, :#{#user.firstName}, "+
           ":#{#user.lastName}, :#{#user.phone}, :#{#user.address}, :#{#user.status}, :#{#user.role}, "+
           ":#{#user.lastLogin}, :#{#user.failedLoginAttempts}, :#{#user.balance}, :#{#user.blockedBalance}, "+
           ":#{#user.reputation}, :#{#user.createdAt}, :#{#user.updatedAt}, :#{#user.version})")
    Mono<Void> insertUser(UserEntity user);
}
