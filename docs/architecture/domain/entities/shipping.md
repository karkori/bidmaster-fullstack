# 📦 Envío (Shipping)

## 📝 Descripción
Gestiona el envío y seguimiento de productos después de una subasta exitosa.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| trackingNumber | String | Número de seguimiento | Único |
| carrier | String | Empresa de transporte | No nulo |

### Detalles
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| method | String | Método de envío | No nulo |
| service | String | Servicio específico | No nulo |
| weight | Decimal | Peso en kg | > 0 |
| dimensions | Dimensions | Dimensiones | No nulo |
| insurance | Boolean | Incluye seguro | Default false |

### Ubicaciones
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| origin | Address | Dirección de origen | No nulo |
| destination | Address | Dirección de destino | No nulo |
| currentLocation | String | Ubicación actual | Opcional |

### Tiempos
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| estimatedDelivery | DateTime | Entrega estimada | No nulo |
| shippedAt | DateTime | Fecha de envío | Opcional |
| deliveredAt | DateTime | Fecha de entrega | Opcional |

### Costos
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| cost | Decimal | Costo del envío | ≥ 0 |
| insuranceCost | Decimal | Costo del seguro | ≥ 0 |
| additionalCosts | Map<String, Decimal> | Costos adicionales | Opcional |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado del envío | Ver ShippingStatus |
| events | List<ShippingEvent> | Historial de eventos | No nulo |
| issues | List<ShippingIssue> | Problemas reportados | Opcional |

## 🔄 Relaciones

### Principales
- **Subasta**: ManyToOne → Auction
- **Vendedor**: ManyToOne → User
- **Comprador**: ManyToOne → User
- **Producto**: ManyToOne → Product

### Secundarias
- **Incidencias**: OneToMany → ShippingIssue
- **Documentos**: OneToMany → ShippingDocument

## 🛡️ Invariantes
1. No puede modificarse después de enviado
2. El tracking debe ser válido para el carrier
3. La fecha estimada debe ser futura
4. El origen y destino deben ser diferentes
5. Los costos deben ser consistentes

## 📊 Value Objects

### ShippingStatus
```typescript
enum ShippingStatus {
    PENDING,      // Pendiente de envío
    READY,        // Listo para enviar
    IN_TRANSIT,   // En tránsito
    DELIVERED,    // Entregado
    RETURNED,     // Devuelto
    LOST,         // Perdido
    DAMAGED       // Dañado
}
```

### Dimensions
```typescript
interface Dimensions {
    length: number;
    width: number;
    height: number;
    unit: 'cm' | 'inch';
}
```

### ShippingEvent
```typescript
interface ShippingEvent {
    id: string;
    status: ShippingStatus;
    location: string;
    timestamp: DateTime;
    description: string;
    metadata: Record<string, any>;
}
```

### ShippingIssue
```typescript
interface ShippingIssue {
    id: string;
    type: 'DELAY' | 'DAMAGE' | 'LOSS' | 'OTHER';
    description: string;
    reportedAt: DateTime;
    resolvedAt?: DateTime;
    resolution?: string;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Shipping {
    // Gestión
    prepare(): void;
    ship(): void;
    updateTracking(event: ShippingEvent): void;
    markDelivered(): void;
    
    // Problemas
    reportIssue(issue: ShippingIssue): void;
    resolveIssue(issueId: string, resolution: string): void;
    
    // Cálculos
    calculateCost(): Decimal;
    estimateDeliveryDate(): DateTime;
    
    // Seguimiento
    getTrackingUrl(): string;
    getCurrentStatus(): ShippingStatus;
    getLastEvent(): ShippingEvent;
}
```

## 🔍 Consultas Comunes
1. Buscar por tracking
2. Filtrar por estado
3. Listar por vendedor/comprador
4. Buscar envíos retrasados
5. Obtener envíos con problemas
6. Calcular estadísticas de entrega
7. Analizar costos promedio
8. Verificar cobertura de servicio

## 📊 Integraciones

### Carriers Soportados
- DHL
- FedEx
- UPS
- Correos
- Local Delivery

### APIs
- Tracking
- Rate Calculation
- Label Generation
- Address Validation
