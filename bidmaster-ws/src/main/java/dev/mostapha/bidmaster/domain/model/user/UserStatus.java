package dev.mostapha.bidmaster.domain.model.user;

/**
 * Enum que representa los posibles estados de un usuario en el sistema.
 */
public enum UserStatus {
    ACTIVE,     // Usuario activo, puede realizar todas las operaciones
    BLOCKED,    // Usuario bloqueado temporalmente (ej. por intentos fallidos de login)
    SUSPENDED,  // Usuario suspendido por un admin (ej. por violación de términos)
    BANNED      // Usuario baneado permanentemente
}
