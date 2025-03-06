# ⭐ Valoración (Rating)

## 📝 Descripción
Representa la valoración y opinión de un usuario sobre otro usuario o una transacción completada.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de valoración | SELLER, BUYER |
| auction | Auction | Subasta relacionada | No nulo |

### Valoración
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| score | Integer | Puntuación | 1-5 |
| title | String | Título breve | 5-100 caracteres |
| comment | String | Comentario detallado | 10-1000 caracteres |
| aspects | Map<String, Integer> | Aspectos específicos | Opcional |

### Participantes
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| reviewer | User | Usuario que valora | No nulo |
| reviewed | User | Usuario valorado | No nulo |
| response | String | Respuesta del valorado | Opcional |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado de la valoración | Ver RatingStatus |
| createdAt | DateTime | Fecha de creación | No nulo |
| updatedAt | DateTime | Última actualización | No nulo |
| responseAt | DateTime | Fecha de respuesta | Opcional |

## 🔄 Relaciones

### Principales
- **Subasta**: ManyToOne → Auction
- **Evaluador**: ManyToOne → User
- **Evaluado**: ManyToOne → User
- **Reportes**: OneToMany → Report

## 🛡️ Invariantes
1. Solo se puede valorar después de completar la transacción
2. Un usuario no puede valorarse a sí mismo
3. Solo se permite una valoración por rol en cada subasta
4. La respuesta solo puede ser añadida por el usuario valorado
5. Las valoraciones no pueden modificarse después de 30 días

## 📊 Value Objects

### RatingType
```typescript
enum RatingType {
    SELLER,    // Valoración al vendedor
    BUYER      // Valoración al comprador
}
```

### RatingStatus
```typescript
enum RatingStatus {
    PENDING,    // Pendiente de moderación
    PUBLISHED,  // Visible públicamente
    HIDDEN,     // Oculta por moderador
    DISPUTED    // En disputa
}
```

### RatingAspects
```typescript
interface RatingAspects {
    communication: number;
    accuracy: number;
    shipping: number;
    value: number;
}
```

### RatingMetrics
```typescript
interface RatingMetrics {
    averageScore: number;
    totalRatings: number;
    distribution: Record<number, number>;
    aspectAverages: Record<string, number>;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Rating {
    // Gestión
    publish(): void;
    hide(reason: string): void;
    addResponse(response: string): void;
    
    // Validaciones
    canBeModified(): boolean;
    isValid(): boolean;
    
    // Métricas
    contributeToScore(): void;
    calculateImpact(): number;
    
    // Moderación
    requiresModeration(): boolean;
    flagInappropriate(): void;
}
```

## 🔍 Consultas Comunes
1. Obtener valoraciones por usuario
2. Filtrar por puntuación
3. Buscar por tipo
4. Listar pendientes de moderación
5. Calcular promedio por usuario
6. Obtener últimas valoraciones
7. Analizar tendencias
