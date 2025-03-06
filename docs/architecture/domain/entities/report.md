# 🚫 Reporte (Report)

## 📝 Descripción
Representa un reporte o denuncia sobre contenido o comportamiento inapropiado.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de reporte | Ver ReportType |
| priority | Enum | Prioridad del reporte | LOW, MEDIUM, HIGH |

### Contenido
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| reason | String | Razón del reporte | No nulo |
| description | String | Descripción detallada | 10-1000 caracteres |
| evidence | List<String> | URLs de evidencia | Opcional |
| category | String | Categoría del reporte | No nulo |

### Participantes
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| reporter | User | Usuario que reporta | No nulo |
| reportedEntity | UUID | Entidad reportada | No nulo |
| reportedEntityType | String | Tipo de entidad | No nulo |
| assignedModerator | User | Moderador asignado | Opcional |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado del reporte | Ver ReportStatus |
| resolution | String | Resolución final | Opcional |
| createdAt | DateTime | Fecha de creación | No nulo |
| updatedAt | DateTime | Última actualización | No nulo |
| resolvedAt | DateTime | Fecha de resolución | Opcional |

## 🔄 Relaciones

### Principales
- **Reportador**: ManyToOne → User
- **Moderador**: ManyToOne → User
- **Entidad Reportada**: ManyToOne → (User/Auction/Product)
- **Notas**: OneToMany → ModeratorNote

## 🛡️ Invariantes
1. No se puede modificar un reporte cerrado
2. Solo moderadores pueden actualizar el estado
3. La resolución es obligatoria al cerrar
4. No se pueden crear reportes duplicados
5. El reportador no puede ser el mismo que el reportado

## 📊 Value Objects

### ReportType
```typescript
enum ReportType {
    INAPPROPRIATE_CONTENT,
    FRAUD,
    HARASSMENT,
    SPAM,
    FAKE_ITEM,
    POLICY_VIOLATION,
    OTHER
}
```

### ReportStatus
```typescript
enum ReportStatus {
    NEW,           // Recién creado
    IN_REVIEW,     // En revisión
    ESCALATED,     // Escalado a superior
    RESOLVED,      // Resuelto
    CLOSED,        // Cerrado sin acción
    INVALID        // Reporte inválido
}
```

### ModeratorNote
```typescript
interface ModeratorNote {
    id: string;
    moderator: User;
    content: string;
    createdAt: DateTime;
    internal: boolean;
}
```

### Resolution
```typescript
interface Resolution {
    action: 'WARNING' | 'SUSPENSION' | 'BAN' | 'CONTENT_REMOVAL' | 'NO_ACTION';
    reason: string;
    duration?: Duration;
    appliedBy: User;
    appliedAt: DateTime;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Report {
    // Gestión de Estado
    assign(moderator: User): void;
    review(): void;
    escalate(reason: string): void;
    resolve(resolution: Resolution): void;
    close(reason: string): void;
    
    // Notas
    addNote(note: ModeratorNote): void;
    updateNote(noteId: string, content: string): void;
    
    // Validaciones
    canBeModified(): boolean;
    requiresImmediate(): boolean;
    isDuplicate(): boolean;
    
    // Acciones
    takeAction(): void;
    notifyParties(): void;
}
```

## 🔍 Consultas Comunes
1. Listar por estado
2. Filtrar por tipo
3. Buscar por entidad reportada
4. Listar por moderador
5. Obtener reportes prioritarios
6. Buscar por fecha
7. Listar reportes sin asignar
