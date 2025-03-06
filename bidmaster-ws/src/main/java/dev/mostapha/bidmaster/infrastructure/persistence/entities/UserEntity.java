package dev.mostapha.bidmaster.infrastructure.persistence.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de persistencia para el usuario del sistema.
 * Representa la tabla de usuarios en la base de datos.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class UserEntity {
    
    // Identificación
    @Id
    private UUID id;

    @NotBlank
    @Size(min = 3, max = 50)
    @Column("username")
    private String username;

    @NotBlank
    @Email
    @Column("email")
    private String email;

    @NotBlank
    @Size(min = 8)
    @Column("password")
    private String password;

    // Información Personal
    @Size(min = 2, max = 50)
    @Column("first_name")
    private String firstName;

    @Size(min = 2, max = 50)
    @Column("last_name")
    private String lastName;
    
    @Column("phone")
    private String phone;
    
    // Dirección (almacenada como JSON en una columna)
    @Column("address")
    private String address;

    // Estado y Seguridad
    @NotNull
    @Column("status")
    private String status; // Enum: ACTIVE, BLOCKED, SUSPENDED, BANNED
    
    @NotNull
    @Column("role")
    private String role; // Enum: USER, ADMIN
    
    @Column("last_login")
    private LocalDateTime lastLogin;
    
    @Min(0)
    @Column("failed_login_attempts")
    private Integer failedLoginAttempts;

    // Finanzas
    @DecimalMin(value = "0.0")
    @Column("balance")
    private BigDecimal balance;
    
    @DecimalMin(value = "0.0")
    @Column("blocked_balance")
    private BigDecimal blockedBalance;
    
    @Min(0)
    @Column("reputation")
    private Integer reputation;
    
    // Campos de auditoría
    @Column("created_at")
    private LocalDateTime createdAt;
    
    @Column("updated_at")
    private LocalDateTime updatedAt;
    
    @Version
    @Column("version")
    private Long version;
}