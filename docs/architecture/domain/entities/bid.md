# 💰 Puja (Bid)

## 📝 Descripción
Representa una oferta realizada por un usuario en una subasta específica.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| auction | Auction | Subasta asociada | No nulo |
| bidder | User | Usuario que realiza la puja | No nulo |

### Valores
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| amount | Decimal | Cantidad de la puja | > 0 |
| increment | Decimal | Incremento sobre puja anterior | ≥ minBidIncrement |
| blockedDeposit | Decimal | Depósito bloqueado | ≥ 0 |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado de la puja | ACTIVE, OUTBID, WINNING |
| createdAt | DateTime | Fecha de creación | No nulo |
| outbidAt | DateTime | Fecha cuando fue superada | Opcional |

## 🔄 Relaciones

### Principales
- **Subasta**: ManyToOne → Auction
- **Pujador**: ManyToOne → User
- **Depósito**: OneToOne → BlockedDeposit

## 🛡️ Invariantes
1. La cantidad debe ser mayor que la puja actual más alta
2. El incremento debe ser igual o mayor al mínimo establecido
3. El depósito bloqueado debe corresponder al porcentaje requerido
4. Un usuario no puede pujar en su propia subasta
5. Solo puede haber una puja ganadora por subasta

## 📊 Value Objects

### BidStatus
```typescript
enum BidStatus {
    ACTIVE,    // Puja actual más alta
    OUTBID,    // Superada por otra puja
    WINNING    // Ganadora (subasta finalizada)
}
```

### BlockedDeposit
```typescript
interface BlockedDeposit {
    amount: Decimal;
    blockedAt: DateTime;
    releasedAt?: DateTime;
    status: 'BLOCKED' | 'RELEASED' | 'APPLIED';
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Bid {
    // Validaciones
    isValid(): boolean;
    isWinning(): boolean;
    wasOutbid(): boolean;
    
    // Gestión de Estado
    markAsOutbid(): void;
    markAsWinning(): void;
    
    // Depósito
    blockDeposit(): boolean;
    releaseDeposit(): boolean;
    applyDeposit(): boolean;
    
    // Cálculos
    calculateRequiredDeposit(): Decimal;
    getIncrementAmount(): Decimal;
}
```

## 🔍 Consultas Comunes
1. Obtener puja más alta por subasta
2. Listar pujas por usuario
3. Listar pujas ganadoras
4. Buscar por rango de fecha
5. Buscar por estado
6. Obtener historial de pujas por subasta
