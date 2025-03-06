# 🔨 Subasta (Auction)

## 📝 Descripción
Representa una subasta activa en el sistema, gestionando el proceso de venta de un producto mediante pujas.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| title | String | Título de la subasta | 5-100 caracteres |
| description | String | Descripción detallada | 20-1000 caracteres |
| slug | String | URL amigable | Único, autogenerado |

### Configuración
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| initialPrice | Decimal | Precio inicial | > 0 |
| currentPrice | Decimal | Precio actual | ≥ initialPrice |
| minBidIncrement | Decimal | Incremento mínimo | > 0 |
| depositRequired | Decimal | Porcentaje de depósito | 0-100 |
| allowPause | Boolean | Permite pausas | - |
| pausesUsed | Integer | Número de pausas usadas | 0-2 |

### Temporización
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| startDate | DateTime | Fecha de inicio | No nulo |
| endDate | DateTime | Fecha de fin | > startDate |
| pauseDate | DateTime | Fecha de pausa | Opcional |
| pauseDuration | Duration | Duración de pausa | ≤ 24h |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado actual | DRAFT, ACTIVE, PAUSED, FINISHED |
| winner | User | Ganador | Opcional |
| totalBids | Integer | Número de pujas | ≥ 0 |
| lastBidDate | DateTime | Última puja | Opcional |

## 🔄 Relaciones

### Principales
- **Producto**: OneToOne → Product
- **Vendedor**: ManyToOne → User
- **Ganador**: ManyToOne → User
- **Pujas**: OneToMany → Bid
- **Categoría**: ManyToOne → Category

### Secundarias
- **Pago**: OneToOne → Payment
- **Reportes**: OneToMany → Report
- **Notificaciones**: OneToMany → Notification

## 🛡️ Invariantes
1. El precio actual siempre debe ser ≥ precio inicial
2. No puede haber pujas después de la fecha de fin
3. Una subasta pausada no puede recibir pujas
4. El número de pausas no puede exceder 2
5. La duración de pausa no puede exceder 24h
6. No se puede modificar una subasta finalizada

## 📊 Value Objects

### AuctionStatus
```typescript
enum AuctionStatus {
    DRAFT,
    ACTIVE,
    PAUSED,
    FINISHED
}
```

### AuctionConfig
```typescript
interface AuctionConfig {
    minBidIncrement: Decimal;
    depositRequired: Decimal;
    allowPause: boolean;
    maxPauses: number;
    maxPauseDuration: Duration;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Auction {
    // Gestión de Estado
    publish(): void;
    pause(): void;
    resume(): void;
    finish(): void;
    
    // Gestión de Pujas
    placeBid(user: User, amount: Decimal): boolean;
    getCurrentWinner(): User;
    getTimeLeft(): Duration;
    
    // Validaciones
    canBid(user: User, amount: Decimal): boolean;
    canPause(): boolean;
    isFinished(): boolean;
    
    // Cálculos
    calculateMinNextBid(): Decimal;
    calculateRequiredDeposit(): Decimal;
}
```

## 🔍 Consultas Comunes
1. Buscar por categoría
2. Buscar por estado
3. Buscar por rango de precio
4. Buscar próximas a finalizar
5. Buscar más populares (más pujas)
6. Buscar por vendedor
