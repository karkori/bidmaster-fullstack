package dev.mostapha.bidmaster.application.port.out;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Puerto del repositorio de usuarios.
 * Define las operaciones que debe implementar un adaptador para interactuar con los usuarios.
 * Siguiendo la arquitectura hexagonal, esta interfaz pertenece al dominio.
 */
public interface UserRepository {
    
    /**
     * Guarda un usuario en el repositorio
     * @param user Usuario a guardar
     * @return El usuario guardado
     */
    Mono<User> save(User user);
    
    /**
     * Busca un usuario por su ID
     * @param id ID del usuario
     * @return El usuario encontrado o Mono.empty() si no existe
     */
    Mono<User> findById(UUID id);
    
    /**
     * Busca un usuario por su nombre de usuario
     * @param username Nombre de usuario
     * @return El usuario encontrado o Mono.empty() si no existe
     */
    Mono<User> findByUsername(String username);
    
    /**
     * Busca un usuario por su correo electrónico
     * @param email Correo electrónico
     * @return El usuario encontrado o Mono.empty() si no existe
     */
    Mono<User> findByEmail(String email);
    
    /**
     * Lista todos los usuarios
     * @return Flux de usuarios
     */
    Flux<User> findAll();
    
    /**
     * Lista usuarios por estado
     * @param status Estado de los usuarios a buscar
     * @return Flux de usuarios con el estado indicado
     */
    Flux<User> findByStatus(UserStatus status);
    
    /**
     * Verifica si existe un usuario con el mismo nombre de usuario
     * @param username Nombre de usuario a verificar
     * @return true si existe, false si no
     */
    Mono<Boolean> existsByUsername(String username);
    
    /**
     * Verifica si existe un usuario con el mismo correo electrónico
     * @param email Correo electrónico a verificar
     * @return true si existe, false si no
     */
    Mono<Boolean> existsByEmail(String email);
    
    /**
     * Elimina un usuario por su ID
     * @param id ID del usuario a eliminar
     * @return Mono que completa cuando se ha eliminado
     */
    Mono<Void> deleteById(UUID id);
    
    /**
     * Actualiza el estado de un usuario
     * @param id ID del usuario
     * @param status Nuevo estado
     * @return Mono que completa cuando se ha actualizado
     */
    Mono<Void> updateStatus(UUID id, UserStatus status);
    
    /**
     * Incrementa el contador de intentos fallidos de login
     * @param id ID del usuario
     * @return Mono que completa cuando se ha incrementado
     */
    Mono<Void> incrementFailedLoginAttempts(UUID id);
    
    /**
     * Resetea el contador de intentos fallidos de login
     * @param id ID del usuario
     * @return Mono que completa cuando se ha reseteado
     */
    Mono<Void> resetFailedLoginAttempts(UUID id);
}
