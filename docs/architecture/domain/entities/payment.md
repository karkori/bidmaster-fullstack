# 💳 Pago (Payment)

## 📝 Descripción
Representa una transacción de pago por una subasta ganada.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| referenceId | String | Referencia externa | Único |
| auction | Auction | Subasta asociada | No nulo |

### Participantes
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| buyer | User | Comprador | No nulo |
| seller | User | Vendedor | No nulo |
| processor | String | Procesador de pago | No nulo |

### Valores
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| amount | Decimal | Monto total | > 0 |
| depositAmount | Decimal | Depósito aplicado | ≥ 0 |
| fee | Decimal | Comisión del sistema | ≥ 0 |
| currency | String | Moneda | ISO 4217 |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado del pago | Ver PaymentStatus |
| createdAt | DateTime | Fecha de creación | No nulo |
| processedAt | DateTime | Fecha de procesamiento | Opcional |
| completedAt | DateTime | Fecha de completado | Opcional |

## 🔄 Relaciones

### Principales
- **Subasta**: ManyToOne → Auction
- **Comprador**: ManyToOne → User
- **Vendedor**: ManyToOne → User
- **Reembolso**: OneToOne → Refund

## 🛡️ Invariantes
1. El monto total debe coincidir con el precio final de la subasta
2. El depósito aplicado no puede exceder el monto total
3. La comisión debe calcularse según las reglas del sistema
4. Solo puede haber un pago exitoso por subasta
5. El comprador y vendedor no pueden ser el mismo usuario

## 📊 Value Objects

### PaymentStatus
```typescript
enum PaymentStatus {
    PENDING,        // Esperando procesamiento
    PROCESSING,     // En proceso
    COMPLETED,      // Pago exitoso
    FAILED,         // Pago fallido
    REFUNDED,       // Reembolsado
    PARTIALLY_REFUNDED // Reembolso parcial
}
```

### PaymentMethod
```typescript
interface PaymentMethod {
    type: 'CREDIT_CARD' | 'DEBIT_CARD' | 'BANK_TRANSFER' | 'CRYPTO';
    provider: string;
    lastFour?: string;
    expiryDate?: string;
}
```

### Refund
```typescript
interface Refund {
    id: string;
    amount: Decimal;
    reason: string;
    status: 'PENDING' | 'COMPLETED' | 'FAILED';
    createdAt: DateTime;
    completedAt?: DateTime;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Payment {
    // Procesamiento
    process(): Promise<boolean>;
    complete(): void;
    fail(reason: string): void;
    
    // Reembolsos
    canBeRefunded(): boolean;
    initiateRefund(amount: Decimal, reason: string): Refund;
    
    // Cálculos
    calculateFee(): Decimal;
    calculateSellerAmount(): Decimal;
    
    // Validaciones
    validateAmount(): boolean;
    validateParticipants(): boolean;
}
```

## 🔍 Consultas Comunes
1. Buscar por referencia
2. Filtrar por estado
3. Listar por comprador/vendedor
4. Buscar por rango de fechas
5. Obtener pagos pendientes
6. Obtener estadísticas de pago
