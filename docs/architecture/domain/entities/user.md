# 👤 Usuario (User)

## 📝 Descripción
Representa a un usuario registrado en el sistema, que puede actuar como comprador o vendedor en las subastas.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| email | String | Correo electrónico | Formato válido, único |
| username | String | Nombre de usuario | 3-50 caracteres, único |
| password | String | Contraseña hasheada | Min 8 caracteres |

### Información Personal
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| firstName | String | Nombre | 2-50 caracteres |
| lastName | String | Apellidos | 2-50 caracteres |
| phone | String | Teléfono | Formato válido |
| address | Address | Dirección | Value Object |

### Estado y Seguridad
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado del usuario | ACTIVE, BLOCKED, SUSPENDED, BANNED |
| role | Enum | Rol en el sistema | USER, ADMIN |
| lastLogin | DateTime | Último acceso | No nulo |
| failedLoginAttempts | Integer | Intentos fallidos | 0-5 |

### Finanzas
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| balance | Decimal | Saldo disponible | ≥ 0 |
| blockedBalance | Decimal | Saldo bloqueado | ≥ 0 |
| reputation | Integer | Puntuación | 0-100 |

## 🔄 Relaciones

### Principales
- **Subastas Creadas**: OneToMany → Auction
- **Pujas Realizadas**: OneToMany → Bid
- **Pagos**: OneToMany → Payment
- **Notificaciones**: OneToMany → Notification

### Secundarias
- **Reportes Enviados**: OneToMany → Report
- **Reportes Recibidos**: OneToMany → Report
- **Registros de Auditoría**: OneToMany → AuditLog

## 🛡️ Invariantes
1. balance + blockedBalance = total balance del usuario
2. No puede pujar si está bloqueado o suspendido
3. No puede crear subastas si está bloqueado o suspendido
4. El balance no puede ser negativo
5. Los intentos fallidos de login no pueden exceder 5

## 📊 Value Objects

### Address
```typescript
interface Address {
    street: string;
    city: string;
    state: string;
    country: string;
    zipCode: string;
}
```

### UserStatus
```typescript
enum UserStatus {
    ACTIVE,
    BLOCKED,
    SUSPENDED,
    BANNED
}
```

### UserRole
```typescript
enum UserRole {
    USER,
    ADMIN
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface User {
    // Finanzas
    blockBalance(amount: Decimal): boolean;
    unblockBalance(amount: Decimal): boolean;
    addBalance(amount: Decimal): void;
    subtractBalance(amount: Decimal): boolean;

    // Seguridad
    incrementFailedLogins(): void;
    resetFailedLogins(): void;
    block(): void;
    unblock(): void;
    suspend(): void;
    ban(): void;

    // Validaciones
    canBid(): boolean;
    canCreateAuction(): boolean;
    hasEnoughBalance(amount: Decimal): boolean;
}
```

## 🔍 Consultas Comunes
1. Buscar por email
2. Buscar por username
3. Listar usuarios por estado
4. Obtener subastas activas
5. Obtener pujas activas
6. Obtener historial de transacciones
