package dev.mostapha.bidmaster.adapter.in.web.controller;

import java.util.List;
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
import dev.mostapha.bidmaster.adapter.in.web.dto.request.LoginRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.request.UserRequestDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.LoginResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.dto.response.UserResponseDTO;
import dev.mostapha.bidmaster.adapter.in.web.mapper.UserMapper;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.config.JwtService;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import jakarta.validation.Valid;

/**
 * Controlador REST para las operaciones relacionadas con usuarios y autenticación JWT.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserUseCase userUseCase;
    private final UserMapper userMapper;
    private final JwtService jwtService;

    public UserController(UserUseCase userUseCase, UserMapper userMapper, JwtService jwtService) {
        this.userUseCase = userUseCase;
        this.userMapper = userMapper;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDTO registerUser(@Valid @RequestBody UserRequestDTO requestDTO) {
        User toRegister = userMapper.toUserForRegistration(requestDTO);
        User registered = userUseCase.registerUser(toRegister);
        return userMapper.toResponseDTO(registered);
    }

    @GetMapping("/{id}")
    public UserResponseDTO getUserById(@PathVariable UUID id) {
        return userUseCase.findUserById(id)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @GetMapping("/by-username/{username}")
    public UserResponseDTO getUserByUsername(@PathVariable String username) {
        return userUseCase.findUserByUsername(username)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @GetMapping("/by-email/{email}")
    public UserResponseDTO getUserByEmail(@PathVariable String email) {
        return userUseCase.findUserByEmail(email)
                .map(userMapper::toResponseDTO)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    @GetMapping
    public List<UserResponseDTO> getAllUsers() {
        return userUseCase.findAllUsers().stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @GetMapping("/by-status/{status}")
    public List<UserResponseDTO> getUsersByStatus(@PathVariable String status) {
        UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
        return userUseCase.findUsersByStatus(userStatus).stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @PutMapping("/{id}")
    public UserResponseDTO updateUser(@PathVariable UUID id, @RequestBody UserRequestDTO requestDTO) {
        User existingUser = userUseCase.findUserById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
        User updated = userMapper.updateUserFromDTO(existingUser, requestDTO);
        return userMapper.toResponseDTO(userUseCase.updateUser(updated));
    }

    @PatchMapping("/{id}/status/{status}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUserStatus(@PathVariable UUID id, @PathVariable String status) {
        UserStatus userStatus = UserStatus.valueOf(status.toUpperCase());
        userUseCase.updateUserStatus(id, userStatus);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable UUID id) {
        userUseCase.deleteUser(id);
    }

    @PostMapping("/{id}/login/success")
    public ResponseEntity<Void> recordSuccessfulLogin(@PathVariable UUID id) {
        return userUseCase.recordSuccessfulLogin(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/{id}/login/failure")
    public ResponseEntity<Void> recordFailedLogin(@PathVariable UUID id) {
        return userUseCase.recordFailedLogin(id)
                ? ResponseEntity.ok().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/{id}/balance/add")
    public ResponseEntity<Void> addBalance(@PathVariable UUID id, @Valid @RequestBody BalanceOperationDTO dto) {
        return userUseCase.addBalance(id, dto.getAmount())
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/balance/subtract")
    public ResponseEntity<Void> subtractBalance(@PathVariable UUID id, @Valid @RequestBody BalanceOperationDTO dto) {
        return userUseCase.subtractBalance(id, dto.getAmount())
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/balance/block")
    public ResponseEntity<Void> blockBalance(@PathVariable UUID id, @Valid @RequestBody BalanceOperationDTO dto) {
        return userUseCase.blockBalance(id, dto.getAmount())
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/{id}/balance/unblock")
    public ResponseEntity<Void> unblockBalance(@PathVariable UUID id, @Valid @RequestBody BalanceOperationDTO dto) {
        return userUseCase.unblockBalance(id, dto.getAmount())
                ? ResponseEntity.ok().build()
                : ResponseEntity.badRequest().build();
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO loginRequest) {
        try {
            User user = userUseCase.login(loginRequest.getUsername(), loginRequest.getPassword());
            String token = jwtService.generateToken(user);

            LoginResponseDTO response = LoginResponseDTO.builder()
                    .id(user.getId().toString())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .firstName(user.getFirstName())
                    .lastName(user.getLastName())
                    .role(user.getRole().name())
                    .token(token)
                    .tokenType("Bearer")
                    .build();

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
