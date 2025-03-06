package dev.mostapha.bidmaster.domain.model.user;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio Usuario.
 * Contiene la lógica de negocio y los invariantes del dominio para el usuario.
 */
public class User {

    // Identificación
    private final UUID id;
    private String username;
    private String email;
    private String password;
    
    // Información Personal
    private String firstName;
    private String lastName;
    private String phone;
    private Address address;
    
    // Estado y Seguridad
    private UserStatus status;
    private UserRole role;
    private LocalDateTime lastLogin;
    private int failedLoginAttempts;
    
    // Finanzas
    private BigDecimal balance;
    private BigDecimal blockedBalance;
    private int reputation;
    
    // Campos de auditoría
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;
    
    /**
     * Constructor para crear un nuevo usuario
     */
    private User(UUID id, String username, String email, String password, UserRole role) {
        this.id = id != null ? id : UUID.randomUUID();
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
        this.status = UserStatus.ACTIVE;
        this.failedLoginAttempts = 0;
        this.balance = BigDecimal.ZERO;
        this.blockedBalance = BigDecimal.ZERO;
        this.reputation = 50; // Reputación inicial
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }
    
    /**
     * Factory method para crear un nuevo usuario
     */
    public static User create(String username, String email, String password, UserRole role) {
        return new User(null, username, email, password, role);
    }
    
    /**
     * Factory method para reconstruir un usuario desde el repositorio
     */
    public static User reconstitute(
            UUID id, 
            String username, 
            String email,
            String password,
            String firstName,
            String lastName,
            String phone,
            Address address,
            UserStatus status,
            UserRole role,
            LocalDateTime lastLogin,
            int failedLoginAttempts,
            BigDecimal balance,
            BigDecimal blockedBalance,
            int reputation,
            LocalDateTime createdAt,
            LocalDateTime updatedAt,
            long version) {
        
        User user = new User(id, username, email, password, role);
        user.firstName = firstName;
        user.lastName = lastName;
        user.phone = phone;
        user.address = address;
        user.status = status;
        user.lastLogin = lastLogin;
        user.failedLoginAttempts = failedLoginAttempts;
        user.balance = balance;
        user.blockedBalance = blockedBalance;
        user.reputation = reputation;
        user.updatedAt = updatedAt;
        user.version = version;
        return user;
    }
    
    // Métodos de comportamiento relacionados con las finanzas
    
    /**
     * Bloquea una cantidad del balance del usuario para una operación pendiente
     * @param amount Cantidad a bloquear
     * @return true si se pudo bloquear, false si no hay saldo suficiente
     */
    public boolean blockBalance(BigDecimal amount) {
        if (!canPerformFinancialOperations()) {
            return false;
        }
        
        if (balance.compareTo(amount) < 0) {
            return false; // No hay saldo suficiente
        }
        
        balance = balance.subtract(amount);
        blockedBalance = blockedBalance.add(amount);
        return true;
    }
    
    /**
     * Desbloquea una cantidad del saldo bloqueado y la devuelve al balance disponible
     * @param amount Cantidad a desbloquear
     * @return true si se pudo desbloquear, false si no hay saldo bloqueado suficiente
     */
    public boolean unblockBalance(BigDecimal amount) {
        if (blockedBalance.compareTo(amount) < 0) {
            return false; // No hay saldo bloqueado suficiente
        }
        
        blockedBalance = blockedBalance.subtract(amount);
        balance = balance.add(amount);
        return true;
    }
    
    /**
     * Añade saldo al balance disponible del usuario
     * @param amount Cantidad a añadir
     */
    public void addBalance(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser positiva");
        }
        
        balance = balance.add(amount);
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Resta saldo del balance disponible del usuario
     * @param amount Cantidad a restar
     * @return true si se pudo restar, false si no hay saldo suficiente
     */
    public boolean subtractBalance(BigDecimal amount) {
        if (!canPerformFinancialOperations()) {
            return false;
        }
        
        if (balance.compareTo(amount) < 0) {
            return false; // No hay saldo suficiente
        }
        
        balance = balance.subtract(amount);
        updatedAt = LocalDateTime.now();
        return true;
    }
    
    // Métodos de comportamiento relacionados con la seguridad
    
    /**
     * Incrementa el contador de intentos fallidos de login
     */
    public void incrementFailedLogins() {
        failedLoginAttempts++;
        updatedAt = LocalDateTime.now();
        
        // Si alcanza el máximo de intentos, se bloquea automáticamente
        if (failedLoginAttempts >= 5) {
            block();
        }
    }
    
    /**
     * Resetea el contador de intentos fallidos de login
     */
    public void resetFailedLogins() {
        failedLoginAttempts = 0;
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Bloquea al usuario
     */
    public void block() {
        status = UserStatus.BLOCKED;
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Desbloquea al usuario
     */
    public void unblock() {
        status = UserStatus.ACTIVE;
        resetFailedLogins();
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Suspende al usuario
     */
    public void suspend() {
        status = UserStatus.SUSPENDED;
        updatedAt = LocalDateTime.now();
    }
    
    /**
     * Banea permanentemente al usuario
     */
    public void ban() {
        status = UserStatus.BANNED;
        updatedAt = LocalDateTime.now();
    }
    
    // Métodos de validación
    
    /**
     * Verifica si el usuario puede realizar pujas
     * @return true si puede pujar, false si no
     */
    public boolean canBid() {
        return status == UserStatus.ACTIVE;
    }
    
    /**
     * Verifica si el usuario puede crear subastas
     * @return true si puede crear subastas, false si no
     */
    public boolean canCreateAuction() {
        return status == UserStatus.ACTIVE;
    }
    
    /**
     * Verifica si el usuario tiene saldo suficiente
     * @param amount Cantidad a verificar
     * @return true si tiene saldo suficiente, false si no
     */
    public boolean hasEnoughBalance(BigDecimal amount) {
        return balance.compareTo(amount) >= 0;
    }
    
    /**
     * Verifica si el usuario puede realizar operaciones financieras
     * @return true si puede realizar operaciones financieras, false si no
     */
    private boolean canPerformFinancialOperations() {
        return status == UserStatus.ACTIVE;
    }
    
    /**
     * Registra un login exitoso
     */
    public void recordSuccessfulLogin() {
        lastLogin = LocalDateTime.now();
        resetFailedLogins();
        updatedAt = LocalDateTime.now();
    }
    
    // Getters y setters
    
    public UUID getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getFirstName() {
        return firstName;
    }
    
    public void setFirstName(String firstName) {
        this.firstName = firstName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getLastName() {
        return lastName;
    }
    
    public void setLastName(String lastName) {
        this.lastName = lastName;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
        this.updatedAt = LocalDateTime.now();
    }
    
    public Address getAddress() {
        return address;
    }
    
    public void setAddress(Address address) {
        this.address = address;
        this.updatedAt = LocalDateTime.now();
    }
    
    public UserStatus getStatus() {
        return status;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getLastLogin() {
        return lastLogin;
    }
    
    public int getFailedLoginAttempts() {
        return failedLoginAttempts;
    }
    
    public BigDecimal getBalance() {
        return balance;
    }
    
    public BigDecimal getBlockedBalance() {
        return blockedBalance;
    }
    
    public int getReputation() {
        return reputation;
    }
    
    public void setReputation(int reputation) {
        this.reputation = reputation;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public long getVersion() {
        return version;
    }
}