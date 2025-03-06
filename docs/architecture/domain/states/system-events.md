# 🎯 Eventos del Sistema

## 📊 Categorías de Eventos

### 🔄 Eventos de Estado
```typescript
interface StateEvents {
    // Subastas
    AuctionCreated: AuctionEvent;
    AuctionStarted: AuctionEvent;
    AuctionEnded: AuctionEvent;
    AuctionPaused: AuctionEvent;
    AuctionCancelled: AuctionEvent;
    
    // Pujas
    BidPlaced: BidEvent;
    BidAccepted: BidEvent;
    BidRejected: BidEvent;
    BidOutbid: BidEvent;
    BidWon: BidEvent;
    
    // Usuarios
    UserRegistered: UserEvent;
    UserVerified: UserEvent;
    UserSuspended: UserEvent;
    UserBanned: UserEvent;
    UserRestored: UserEvent;
    
    // Pagos
    PaymentInitiated: PaymentEvent;
    PaymentProcessing: PaymentEvent;
    PaymentCompleted: PaymentEvent;
    PaymentFailed: PaymentEvent;
    PaymentRefunded: PaymentEvent;
}
```

## 🔄 Estructura de Eventos

### Base Event
```typescript
interface BaseEvent {
    id: string;
    type: EventType;
    timestamp: DateTime;
    actor: {
        id: string;
        type: ActorType;
    };
    metadata: {
        correlationId: string;
        causationId: string;
        version: string;
    };
}
```

### Eventos Específicos
```typescript
interface AuctionEvent extends BaseEvent {
    auctionId: string;
    status: AuctionStatus;
    details: {
        currentPrice?: number;
        bidCount?: number;
        timeRemaining?: number;
    };
}

interface BidEvent extends BaseEvent {
    bidId: string;
    auctionId: string;
    amount: number;
    status: BidStatus;
}

interface PaymentEvent extends BaseEvent {
    paymentId: string;
    amount: number;
    currency: string;
    status: PaymentStatus;
}
```

## 📱 Suscriptores

### Por Dominio
```typescript
interface EventSubscribers {
    Auction: {
        NotificationService: ['created', 'started', 'ended'];
        SearchService: ['created', 'updated', 'ended'];
        AnalyticsService: ['all'];
    };
    
    User: {
        AuthService: ['registered', 'verified', 'banned'];
        NotificationService: ['all'];
        AuditService: ['all'];
    };
    
    Payment: {
        AccountingService: ['completed', 'refunded'];
        NotificationService: ['all'];
        FraudService: ['all'];
    };
}
```

## 🔄 Acciones Automáticas

### Por Evento
```typescript
interface AutomatedActions {
    AuctionEnded: [
        'notifyParticipants',
        'processWinningBid',
        'updateInventory',
        'generateReports'
    ];
    
    PaymentCompleted: [
        'releaseProduct',
        'notifyParties',
        'updateBalances',
        'triggerShipment'
    ];
    
    UserSuspended: [
        'pauseActiveAuctions',
        'holdPayments',
        'notifyContacts',
        'logAuditTrail'
    ];
}
```

## 📨 Notificaciones

### Configuración
```typescript
interface NotificationConfig {
    Priority: {
        High: ['payment', 'security', 'auction_end'];
        Medium: ['bid', 'auction_update', 'shipping'];
        Low: ['system', 'marketing', 'reminder'];
    };
    
    Channels: {
        Email: ['all'];
        Push: ['high_priority', 'medium_priority'];
        SMS: ['high_priority'];
        InApp: ['all'];
    };
    
    Templates: {
        byEvent: Record<EventType, NotificationTemplate>;
        byLanguage: Record<string, Record<EventType, string>>;
    };
}
```

## 📊 Monitoreo

### Métricas
```typescript
interface EventMetrics {
    Performance: {
        processingTime: number;
        queueLength: number;
        errorRate: number;
        retryCount: number;
    };
    
    Volume: {
        eventsByType: Record<EventType, number>;
        eventsByHour: number[];
        peakVolume: number;
    };
    
    Health: {
        subscriberStatus: Record<string, Status>;
        queueHealth: QueueMetrics;
        systemLoad: SystemMetrics;
    };
}
```

## 🔍 Queries

### Event Sourcing
```typescript
interface EventQueries {
    // Reconstrucción de estado
    getEntityState(entityId: string, timestamp?: DateTime): Promise<State>;
    
    // Auditoría
    getEventTrail(params: {
        entityId?: string;
        eventType?: EventType;
        dateRange?: DateRange;
    }): Promise<Event[]>;
    
    // Analytics
    getEventStats(params: {
        types: EventType[];
        period: TimePeriod;
        metrics: MetricType[];
    }): Promise<EventStats>;
}
```

## 🔒 Seguridad

### Políticas
```typescript
interface SecurityPolicies {
    EventAccess: {
        roles: Record<Role, EventType[]>;
        encryption: Record<EventType, EncryptionLevel>;
        retention: Record<EventType, Duration>;
    };
    
    Validation: {
        schema: Record<EventType, ValidationSchema>;
        rules: Record<EventType, ValidationRule[]>;
    };
    
    Audit: {
        logLevel: Record<EventType, LogLevel>;
        retention: Duration;
    };
}
```

## 🔄 Recuperación

### Estrategias
```typescript
interface RecoveryStrategies {
    EventReplay: {
        batchSize: number;
        parallel: boolean;
        validation: boolean;
    };
    
    ErrorHandling: {
        retryPolicy: RetryPolicy;
        fallbackActions: Record<EventType, Action>;
        compensation: Record<EventType, Action>;
    };
    
    Consistency: {
        checkpoints: CheckpointConfig;
        validation: ValidationConfig;
        repair: RepairStrategy;
    };
}
```
