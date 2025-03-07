package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.mostapha.bidmaster.adapter.in.web.dto.BalanceOperationDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.UserResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.request.LoginRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.request.UserRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.LoginResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.mapper.UserMapper;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Controlador REST para las operaciones relacionadas con usuarios.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;

    public UserController(UserUseCase userUseCase, UserMapper userMapper) {
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
    }

    /**
     * Registra un nuevo usuario.
     */
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public Mono<UserResponseDTO> registerUser(@RequestBody UserRequestDTO requestDTO) {
        return Mono.just(requestDTO)
                .map(userMapper::toUserForRegistration)
                .flatMap(userUseCase::registerUser)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Busca un usuario por su ID.
     */
    @GetMapping("/{id}")
    public Mono<UserResponseDTO> getUserById(@PathVariable UUID id) {
        return userUseCase.findUserById(id)
                .map(userMapper::toResponseDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")));
    }

    /**
     * Busca un usuario por su nombre de usuario.
     */
    @GetMapping("/by-username/{username}")
    public Mono<UserResponseDTO> getUserByUsername(@PathVariable String username) {
        return userUseCase.findUserByUsername(username)
                .map(userMapper::toResponseDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")));
    }

    /**
     * Busca un usuario por su correo electrónico.
     */
    @GetMapping("/by-email/{email}")
    public Mono<UserResponseDTO> getUserByEmail(@PathVariable String email) {
        return userUseCase.findUserByEmail(email)
                .map(userMapper::toResponseDTO)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")));
    }

    /**
     * Lista todos los usuarios.
     */
    @GetMapping
    public Flux<UserResponseDTO> getAllUsers() {
        return userUseCase.findAllUsers()
                .map(userMapper::toResponseDTO);
    }

    /**
     * Lista usuarios por estado.
     */
    @GetMapping("/by-status/{status}")
    public Flux<UserResponseDTO> getUsersByStatus(@PathVariable String status) {
        UserStatus userStatus = UserStatus.valueOf(status);
        return userUseCase.findUsersByStatus(userStatus)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Actualiza la información de un usuario.
     */
    @PutMapping("/{id}")
    public Mono<UserResponseDTO> updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO requestDTO) {
        return userUseCase.findUserById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .map(existingUser -> userMapper.updateUserFromDTO(existingUser, requestDTO))
                .flatMap(userUseCase::updateUser)
                .map(userMapper::toResponseDTO);
    }

    /**
     * Actualiza el estado de un usuario.
     */
    @PatchMapping("/{id}/status/{status}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> updateUserStatus(@PathVariable UUID id, @PathVariable String status) {
        try {
            UserStatus userStatus = UserStatus.valueOf(status);
            return userUseCase.updateUserStatus(id, userStatus);
        } catch (IllegalArgumentException e) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Estado de usuario no válido"));
        }
    }

    /**
     * Elimina un usuario.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteUser(@PathVariable UUID id) {
        return userUseCase.deleteUser(id);
    }

    /**
     * Registra un inicio de sesión exitoso.
     */
    @PostMapping("/{id}/login/success")
    public Mono<ResponseEntity<Void>> recordSuccessfulLogin(@PathVariable UUID id) {
        return userUseCase.recordSuccessfulLogin(id)
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Registra un intento de inicio de sesión fallido.
     */
    @PostMapping("/{id}/login/failure")
    public Mono<ResponseEntity<Void>> recordFailedLogin(@PathVariable UUID id) {
        return userUseCase.recordFailedLogin(id)
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado")))
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()));
    }

    /**
     * Añade saldo al balance disponible del usuario.
     */
    @PostMapping("/{id}/balance/add")
    public Mono<ResponseEntity<Void>> addBalance(@PathVariable UUID id, @RequestBody BalanceOperationDTO dto) {
        return userUseCase.addBalance(id, dto.getAmount())
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "No se pudo añadir el saldo")))
                .onErrorResume(e -> {
                    if (e instanceof IllegalArgumentException) {
                        return Mono.just(ResponseEntity.badRequest().<Void>build());
                    }
                    return Mono.just(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());
                });
    }

    /**
     * Resta saldo del balance disponible del usuario.
     */
    @PostMapping("/{id}/balance/subtract")
    public Mono<ResponseEntity<Void>> subtractBalance(@PathVariable UUID id, @RequestBody BalanceOperationDTO dto) {
        return userUseCase.subtractBalance(id, dto.getAmount())
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }

    /**
     * Bloquea una cantidad del balance para una operación pendiente.
     */
    @PostMapping("/{id}/balance/block")
    public Mono<ResponseEntity<Void>> blockBalance(@PathVariable UUID id, @RequestBody BalanceOperationDTO dto) {
        return userUseCase.blockBalance(id, dto.getAmount())
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }

    /**
     * Endpoint para autenticar un usuario.
     * 
     * @param loginRequest Credenciales de login (username/email y password)
     * @return Datos del usuario autenticado junto con un token JWT
     */
    @PostMapping("/login")
    public Mono<ResponseEntity<LoginResponseDTO>> login(@RequestBody LoginRequestDTO loginRequest) {
        return userUseCase.login(loginRequest.getUsername(), loginRequest.getPassword())
                .map(user -> {
                    // Crear respuesta con token JWT (en una implementación real se generaría un
                    // token)
                    LoginResponseDTO response = LoginResponseDTO.builder()
                            .id(user.getId().toString())
                            .username(user.getUsername())
                            .email(user.getEmail())
                            .firstName(user.getFirstName())
                            .lastName(user.getLastName())
                            .role(user.getRole().toString())
                            .token("jwt_token_placeholder")
                            .tokenType("Bearer")
                            .build();

                    return ResponseEntity.ok(response);
                })
                .onErrorResume(IllegalArgumentException.class,
                        e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body((LoginResponseDTO) null)));
    }

    /**
     * Desbloquea una cantidad del balance bloqueado.
     */
    @PostMapping("/{id}/balance/unblock")
    public Mono<ResponseEntity<Void>> unblockBalance(@PathVariable UUID id, @RequestBody BalanceOperationDTO dto) {
        return userUseCase.unblockBalance(id, dto.getAmount())
                .flatMap(success -> success ? Mono.just(ResponseEntity.ok().<Void>build())
                        : Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).build()));
    }
}
