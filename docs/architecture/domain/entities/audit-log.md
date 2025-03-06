# 📝 Registro de Auditoría (AuditLog)

## 📝 Descripción
Registra todas las acciones y cambios significativos en el sistema para auditoría y seguimiento.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de acción | Ver AuditType |
| severity | Enum | Severidad del evento | INFO, WARNING, ERROR |

### Evento
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| action | String | Acción realizada | No nulo |
| entityType | String | Tipo de entidad | No nulo |
| entityId | UUID | ID de entidad | No nulo |
| timestamp | DateTime | Momento del evento | No nulo |

### Cambios
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| previousState | JSON | Estado anterior | Opcional |
| newState | JSON | Nuevo estado | Opcional |
| changes | JSON | Diferencias | Opcional |
| metadata | JSON | Metadatos adicionales | Opcional |

### Contexto
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| actor | User | Usuario que realizó la acción | No nulo |
| ipAddress | String | Dirección IP | No nulo |
| userAgent | String | User Agent | No nulo |
| sessionId | String | ID de sesión | No nulo |

## 🔄 Relaciones

### Principales
- **Usuario**: ManyToOne → User
- **Entidad**: ManyToOne → (Any Entity)

## 🛡️ Invariantes
1. Los registros de auditoría son inmutables
2. Cada registro debe tener un actor identificado
3. El timestamp no puede ser futuro
4. Los cambios deben ser serializables
5. Debe mantenerse la integridad referencial

## 📊 Value Objects

### AuditType
```typescript
enum AuditType {
    CREATE,           // Creación de entidad
    UPDATE,           // Modificación de entidad
    DELETE,           // Eliminación de entidad
    ACCESS,           // Acceso a datos
    AUTHENTICATION,   // Eventos de autenticación
    AUTHORIZATION,    // Cambios de permisos
    CONFIGURATION,    // Cambios de configuración
    BUSINESS_PROCESS  // Procesos de negocio
}
```

### AuditSeverity
```typescript
enum AuditSeverity {
    INFO,     // Cambios normales
    WARNING,  // Acciones inusuales
    ERROR     // Problemas o violaciones
}
```

### StateChange
```typescript
interface StateChange {
    field: string;
    previousValue: any;
    newValue: any;
    timestamp: DateTime;
}
```

### AuditMetadata
```typescript
interface AuditMetadata {
    environment: string;
    component: string;
    correlationId: string;
    tags: string[];
    custom: Record<string, any>;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface AuditLog {
    // Creación
    static create(event: AuditEvent): AuditLog;
    static createBatch(events: AuditEvent[]): AuditLog[];
    
    // Consulta
    getChanges(): StateChange[];
    hasChanged(field: string): boolean;
    getDuration(): Duration;
    
    // Exportación
    toJSON(): Record<string, any>;
    toCSV(): string;
    
    // Análisis
    isSignificant(): boolean;
    requiresAttention(): boolean;
    getSummary(): string;
}
```

## 🔍 Consultas Comunes
1. Buscar por tipo de acción
2. Filtrar por entidad
3. Buscar por usuario
4. Filtrar por severidad
5. Buscar por rango de fechas
6. Obtener cambios específicos
7. Agrupar por tipo de entidad
8. Análisis de tendencias
