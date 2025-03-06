# 🔔 Notificación (Notification)

## 📝 Descripción
Representa una notificación enviada a un usuario sobre eventos del sistema.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de notificación | Ver NotificationType |
| recipient | User | Usuario destinatario | No nulo |

### Contenido
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| title | String | Título corto | 5-100 caracteres |
| message | String | Mensaje completo | 10-500 caracteres |
| data | JSON | Datos adicionales | Opcional |
| priority | Enum | Prioridad | LOW, MEDIUM, HIGH |

### Enlaces
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| actionUrl | String | URL de acción | URL válida |
| imageUrl | String | URL de imagen | URL válida, opcional |
| entityType | String | Tipo de entidad relacionada | No nulo |
| entityId | UUID | ID de entidad relacionada | No nulo |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado de la notificación | Ver NotificationStatus |
| createdAt | DateTime | Fecha de creación | No nulo |
| readAt | DateTime | Fecha de lectura | Opcional |
| expiresAt | DateTime | Fecha de expiración | Opcional |

## 🔄 Relaciones

### Principales
- **Usuario**: ManyToOne → User
- **Entidad Relacionada**: ManyToOne → (Auction/Bid/Payment)

## 🛡️ Invariantes
1. Una notificación no puede modificarse después de leída
2. Las notificaciones expiradas no se muestran
3. El usuario debe tener permisos para la entidad relacionada
4. Las notificaciones de alta prioridad no expiran
5. Debe mantenerse un límite de notificaciones por usuario

## 📊 Value Objects

### NotificationType
```typescript
enum NotificationType {
    AUCTION_START,
    AUCTION_END,
    NEW_BID,
    OUTBID,
    AUCTION_WON,
    PAYMENT_REQUIRED,
    PAYMENT_RECEIVED,
    SYSTEM_ALERT,
    ACCOUNT_UPDATE
}
```

### NotificationStatus
```typescript
enum NotificationStatus {
    PENDING,    // Creada pero no enviada
    SENT,       // Enviada al usuario
    DELIVERED,  // Confirmada la entrega
    READ,       // Leída por el usuario
    EXPIRED,    // Expirada sin leer
    FAILED      // Error en el envío
}
```

### NotificationPriority
```typescript
enum NotificationPriority {
    LOW,      // Informativa
    MEDIUM,   // Importante
    HIGH      // Crítica/Urgente
}
```

### NotificationChannel
```typescript
interface NotificationChannel {
    type: 'EMAIL' | 'PUSH' | 'SMS' | 'IN_APP';
    enabled: boolean;
    settings: Record<string, any>;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Notification {
    // Gestión de Estado
    send(): Promise<boolean>;
    markAsDelivered(): void;
    markAsRead(): void;
    expire(): void;
    
    // Validaciones
    canBeSent(): boolean;
    isExpired(): boolean;
    shouldResend(): boolean;
    
    // Canales
    getEnabledChannels(): NotificationChannel[];
    shouldSendToChannel(channel: string): boolean;
    
    // Contenido
    render(): string;
    getActionData(): Record<string, any>;
}
```

## 🔍 Consultas Comunes
1. Listar no leídas por usuario
2. Filtrar por tipo
3. Buscar por entidad relacionada
4. Listar por prioridad
5. Obtener notificaciones recientes
6. Contar notificaciones pendientes
