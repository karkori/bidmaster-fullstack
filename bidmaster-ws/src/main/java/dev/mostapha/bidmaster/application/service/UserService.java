package dev.mostapha.bidmaster.application.service;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.stereotype.Service;

import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Implementación del caso de uso de usuario.
 * Coordina entre el dominio y los puertos de salida, orquestando las
 * operaciones
 * pero delegando la lógica de negocio a las entidades del dominio.
 */
@Service
public class UserService implements UserUseCase {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public Mono<User> registerUser(User user) {
        // Las validaciones de negocio se delegan al dominio User
        // Aquí solo verificamos restricciones a nivel de infraestructura (unicidad)
        return Mono.just(user)
                .filterWhen(u -> userRepository.existsByUsername(u.getUsername()).map(exists -> !exists))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El nombre de usuario ya está en uso")))
                .filterWhen(u -> userRepository.existsByEmail(u.getEmail()).map(exists -> !exists))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("El correo electrónico ya está en uso")))
                .flatMap(userRepository::save);
    }

    @Override
    public Mono<User> findUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Mono<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Mono<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public Flux<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Override
    public Flux<User> findUsersByStatus(UserStatus status) {
        return userRepository.findByStatus(status);
    }

    @Override
    public Mono<User> updateUser(User updatedUser) {
        return userRepository.findById(updatedUser.getId())
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(existingUser -> {
                    // Actualizar propiedades usando los métodos del dominio para mantener la lógica
                    // de negocio
                    existingUser.setUsername(updatedUser.getUsername());
                    existingUser.setEmail(updatedUser.getEmail());
                    existingUser.setFirstName(updatedUser.getFirstName());
                    existingUser.setLastName(updatedUser.getLastName());
                    existingUser.setPhone(updatedUser.getPhone());
                    existingUser.setAddress(updatedUser.getAddress());
                    existingUser.setRole(updatedUser.getRole());

                    // No actualizamos password, balance, ni estado directamente - esos tienen
                    // métodos específicos

                    return userRepository.save(existingUser);
                });
    }

    @Override
    public Mono<Void> updateUserStatus(UUID id, UserStatus status) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Utilizamos los métodos del dominio que encapsulan la lógica de cambio de
                    // estado
                    switch (status) {
                        case ACTIVE:
                            user.unblock(); // Este método ya tiene la lógica para activar y resetear intentos
                            break;
                        case BLOCKED:
                            user.block(); // Este método tiene la lógica para bloquear al usuario
                            break;
                        case SUSPENDED:
                            user.suspend(); // Este método tiene la lógica para suspender al usuario
                            break;
                        case BANNED:
                            user.ban(); // Este método tiene la lógica para banear al usuario
                            break;
                    }
                    return userRepository.save(user);
                })
                .then();
    }

    @Override
    public Mono<Void> deleteUser(UUID id) {
        return userRepository.deleteById(id);
    }

    @Override
    public Mono<Boolean> recordSuccessfulLogin(UUID id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Usamos el método del dominio para registrar el login exitoso
                    user.recordSuccessfulLogin();
                    return userRepository.save(user);
                })
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> recordFailedLogin(UUID id) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Usamos el método del dominio para registrar el intento fallido
                    // Este método ya incluye la lógica de bloqueo automático tras varios intentos
                    user.incrementFailedLogins();
                    return userRepository.save(user);
                })
                .thenReturn(true);
    }

    @Override
    public Mono<Boolean> addBalance(UUID id, BigDecimal amount) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    try {
                        // Usamos el método del dominio que ya valida que la cantidad sea positiva
                        user.addBalance(amount);
                        return userRepository.save(user).thenReturn(true);
                    } catch (IllegalArgumentException e) {
                        return Mono.error(e);
                    }
                });
    }

    @Override
    public Mono<Boolean> subtractBalance(UUID id, BigDecimal amount) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Usamos el método del dominio que ya valida saldo suficiente y estado activo
                    boolean success = user.subtractBalance(amount);
                    if (success) {
                        return userRepository.save(user).thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<Boolean> blockBalance(UUID id, BigDecimal amount) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Usamos el método del dominio que ya valida saldo suficiente y estado activo
                    boolean success = user.blockBalance(amount);
                    if (success) {
                        return userRepository.save(user).thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<Boolean> unblockBalance(UUID id, BigDecimal amount) {
        return userRepository.findById(id)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Usamos el método del dominio que ya valida saldo bloqueado suficiente
                    boolean success = user.unblockBalance(amount);
                    if (success) {
                        return userRepository.save(user).thenReturn(true);
                    } else {
                        return Mono.just(false);
                    }
                });
    }

    @Override
    public Mono<User> login(String usernameOrEmail, String password) {
        // Primero intentamos encontrar por nombre de usuario
        return userRepository.findByUsername(usernameOrEmail)
                .switchIfEmpty(
                        // Si no encontramos por username, intentamos por email
                        userRepository.findByEmail(usernameOrEmail))
                .switchIfEmpty(
                        Mono.error(new IllegalArgumentException("Usuario no encontrado")))
                .flatMap(user -> {
                    // Verificamos si la contraseña coincide
                    if (user.isValidPassword(password)) {
                        // Registramos el login exitoso
                        user.recordSuccessfulLogin();
                        return userRepository.save(user);
                    } else {
                        // Registramos el intento fallido de login
                        user.incrementFailedLogins();
                        userRepository.save(user).subscribe(); // Guardar en segundo plano
                        return Mono.error(new IllegalArgumentException("Contraseña incorrecta"));
                    }
                });
    }
}
