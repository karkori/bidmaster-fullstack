package dev.mostapha.bidmaster.application.port.in;

import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Puerto de entrada para las operaciones relacionadas con usuarios.
 * Define los casos de uso disponibles para interactuar con los usuarios.
 */
public interface UserUseCase {
    
    /**
     * Registra un nuevo usuario en el sistema
     */
    Mono<User> registerUser(User user);
    
    /**
     * Busca un usuario por su ID
     */
    Mono<User> findUserById(UUID id);
    
    /**
     * Busca un usuario por su nombre de usuario
     */
    Mono<User> findUserByUsername(String username);
    
    /**
     * Busca un usuario por su correo electrónico
     */
    Mono<User> findUserByEmail(String email);
    
    /**
     * Lista todos los usuarios
     */
    Flux<User> findAllUsers();
    
    /**
     * Lista usuarios por estado
     */
    Flux<User> findUsersByStatus(UserStatus status);
    
    /**
     * Actualiza la información de un usuario
     */
    Mono<User> updateUser(User user);
    
    /**
     * Actualiza el estado de un usuario
     */
    Mono<Void> updateUserStatus(UUID id, UserStatus status);
    
    /**
     * Elimina un usuario por su ID
     */
    Mono<Void> deleteUser(UUID id);
    
    /**
     * Registra un inicio de sesión exitoso, actualizando lastLogin y reseteando intentos fallidos
     */
    Mono<Boolean> recordSuccessfulLogin(UUID id);
    
    /**
     * Registra un intento de inicio de sesión fallido, incrementando el contador
     * y posiblemente bloqueando al usuario si excede el límite
     */
    Mono<Boolean> recordFailedLogin(UUID id);
    
    /**
     * Añade saldo al balance disponible del usuario
     */
    Mono<Boolean> addBalance(UUID id, BigDecimal amount);
    
    /**
     * Resta saldo del balance disponible del usuario si tiene suficiente y está activo
     */
    Mono<Boolean> subtractBalance(UUID id, BigDecimal amount);
    
    /**
     * Bloquea una cantidad del balance para una operación pendiente
     */
    Mono<Boolean> blockBalance(UUID id, BigDecimal amount);
    
    /**
     * Desbloquea una cantidad del saldo bloqueado y la devuelve al balance disponible
     */
    Mono<Boolean> unblockBalance(UUID id, BigDecimal amount);
}
