# ⚙️ Configuración del Sistema (SystemConfig)

## 📝 Descripción
Gestiona la configuración y parametrización del sistema.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| key | String | Clave de configuración | No nulo, único |
| environment | String | Entorno de aplicación | No nulo |

### Valor
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| value | String | Valor de configuración | No nulo |
| type | Enum | Tipo de dato | Ver ConfigType |
| encrypted | Boolean | Indica si está cifrado | Default false |
| sensitive | Boolean | Dato sensible | Default false |

### Metadatos
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| description | String | Descripción del parámetro | No nulo |
| category | String | Categoría de configuración | No nulo |
| tags | Set<String> | Etiquetas | Opcional |
| validation | String | Regla de validación | Opcional |

### Auditoría
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| version | Integer | Versión del valor | ≥ 0 |
| createdAt | DateTime | Fecha de creación | No nulo |
| updatedAt | DateTime | Última actualización | No nulo |
| updatedBy | User | Usuario que actualizó | No nulo |

## 🔄 Relaciones

### Principales
- **Usuario**: ManyToOne → User
- **Historial**: OneToMany → ConfigHistory

## 🛡️ Invariantes
1. Las claves deben ser únicas por entorno
2. Los valores deben cumplir su tipo de dato
3. Los valores sensibles deben estar cifrados
4. Debe mantenerse el historial de cambios
5. Solo administradores pueden modificar la configuración

## 📊 Value Objects

### ConfigType
```typescript
enum ConfigType {
    STRING,
    NUMBER,
    BOOLEAN,
    JSON,
    DATE,
    EMAIL,
    URL,
    ENUM
}
```

### ConfigEnvironment
```typescript
enum ConfigEnvironment {
    DEVELOPMENT,
    TESTING,
    STAGING,
    PRODUCTION
}
```

### ConfigHistory
```typescript
interface ConfigHistory {
    id: string;
    previousValue: string;
    newValue: string;
    changedAt: DateTime;
    changedBy: User;
    reason: string;
}
```

### ValidationRule
```typescript
interface ValidationRule {
    type: string;
    pattern?: string;
    min?: number;
    max?: number;
    options?: string[];
    custom?: string;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface SystemConfig {
    // Gestión de Valores
    setValue(value: any, user: User): void;
    getValue<T>(): T;
    reset(): void;
    
    // Validación
    validate(): boolean;
    isValid(value: any): boolean;
    
    // Cifrado
    encrypt(): void;
    decrypt(): string;
    
    // Historial
    getHistory(): ConfigHistory[];
    revertTo(version: number): void;
    
    // Utilidades
    toString(): string;
    toJSON(): Record<string, any>;
}
```

## 🔍 Consultas Comunes
1. Buscar por clave
2. Filtrar por categoría
3. Listar por entorno
4. Buscar configuraciones sensibles
5. Obtener historial de cambios
6. Listar por tipo
7. Buscar por tags
8. Verificar configuraciones inválidas

## 📋 Configuraciones Comunes

### Subastas
- `auction.min_bid_increment`
- `auction.max_duration`
- `auction.auto_extend_time`
- `auction.deposit_percentage`

### Sistema
- `system.maintenance_mode`
- `system.version`
- `system.api_rate_limit`
- `system.session_timeout`

### Notificaciones
- `notification.email_enabled`
- `notification.sms_enabled`
- `notification.batch_size`
- `notification.retry_count`

### Seguridad
- `security.password_policy`
- `security.login_attempts`
- `security.jwt_secret`
- `security.cors_origins`
