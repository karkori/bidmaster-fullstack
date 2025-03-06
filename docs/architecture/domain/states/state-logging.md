# 📝 Registro de Estados

## 📊 Estructura de Logs

### Log Entry Base
```typescript
interface BaseStateLog {
    id: string;
    timestamp: DateTime;
    entity: {
        id: string;
        type: EntityType;
    };
    state: {
        from: string;
        to: string;
    };
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

### Logs Específicos
```typescript
interface AuctionStateLog extends BaseStateLog {
    details: {
        currentPrice?: number;
        bidCount?: number;
        reason?: string;
        affectedBids?: string[];
    };
}

interface UserStateLog extends BaseStateLog {
    details: {
        reason?: string;
        duration?: number;
        restrictions?: string[];
        appeals?: AppealInfo[];
    };
}

interface PaymentStateLog extends BaseStateLog {
    details: {
        amount?: number;
        method?: string;
        errorCode?: string;
        transactionId?: string;
    };
}
```

## 🔍 Auditoría

### Campos Auditados
```typescript
interface AuditFields {
    Mandatory: [
        'timestamp',
        'actor',
        'action',
        'entityId',
        'stateChange'
    ];
    
    Conditional: {
        Security: [
            'ipAddress',
            'userAgent',
            'location'
        ];
        Financial: [
            'amount',
            'currency',
            'balance'
        ];
        Compliance: [
            'documents',
            'approvals',
            'checks'
        ];
    };
}
```

## 📊 Histórico

### Estructura
```typescript
interface StateHistory {
    entity: {
        id: string;
        type: EntityType;
        created: DateTime;
    };
    
    transitions: {
        timestamp: DateTime;
        fromState: string;
        toState: string;
        reason: string;
        actor: ActorInfo;
    }[];
    
    metadata: {
        totalTransitions: number;
        averageDuration: Duration;
        commonStates: string[];
    };
}
```

## 🎯 Razones de Cambio

### Categorización
```typescript
interface StateChangeReasons {
    System: [
        'scheduled',
        'automated',
        'maintenance',
        'recovery'
    ];
    
    User: [
        'requested',
        'cancelled',
        'modified',
        'approved'
    ];
    
    Admin: [
        'moderation',
        'support',
        'security',
        'compliance'
    ];
    
    Error: [
        'validation',
        'timeout',
        'system_error',
        'external_error'
    ];
}
```

## 👥 Responsables

### Tipos de Actor
```typescript
interface ActorTypes {
    System: {
        type: 'SYSTEM';
        process: string;
        component: string;
    };
    
    User: {
        type: 'USER';
        id: string;
        role: string;
    };
    
    Admin: {
        type: 'ADMIN';
        id: string;
        permissions: string[];
    };
    
    Service: {
        type: 'SERVICE';
        name: string;
        version: string;
    };
}
```

## 📱 Notificaciones

### Configuración
```typescript
interface LogNotifications {
    Critical: {
        states: string[];
        recipients: string[];
        channels: string[];
        immediate: boolean;
    };
    
    Security: {
        patterns: string[];
        team: string[];
        escalation: string[];
    };
    
    Audit: {
        schedule: string;
        format: string;
        retention: string;
    };
}
```

## 🔍 Queries

### Búsqueda
```typescript
interface LogQueries {
    // Búsqueda por entidad
    getEntityHistory(params: {
        entityId: string;
        dateRange?: DateRange;
        states?: string[];
    }): Promise<StateLog[]>;
    
    // Búsqueda por actor
    getActorActions(params: {
        actorId: string;
        actionTypes?: string[];
        dateRange?: DateRange;
    }): Promise<StateLog[]>;
    
    // Análisis de patrones
    getStatePatterns(params: {
        entityType: string;
        period: TimePeriod;
        groupBy: string[];
    }): Promise<PatternAnalysis>;
}
```

## 📊 Reportes

### Tipos
```typescript
interface LogReports {
    Compliance: {
        stateChanges: StateChangeReport;
        actorActions: ActorReport;
        securityEvents: SecurityReport;
    };
    
    Analytics: {
        transitionTimes: TimeReport;
        commonPatterns: PatternReport;
        anomalies: AnomalyReport;
    };
    
    Audit: {
        accessLog: AccessReport;
        changeLog: ChangeReport;
        errorLog: ErrorReport;
    };
}
```

## 🔒 Seguridad

### Políticas
```typescript
interface LogSecurity {
    Access: {
        roles: Record<Role, Permission[]>;
        encryption: EncryptionConfig;
        masking: MaskingRules;
    };
    
    Retention: {
        regular: Duration;
        sensitive: Duration;
        archived: Duration;
    };
    
    Compliance: {
        gdpr: GDPRConfig;
        audit: AuditConfig;
        reporting: ReportingConfig;
    };
}
```

## 🔄 Mantenimiento

### Tareas
```typescript
interface LogMaintenance {
    Cleanup: {
        schedule: string;
        criteria: CleanupCriteria;
        backup: BackupConfig;
    };
    
    Archival: {
        schedule: string;
        format: string;
        storage: StorageConfig;
    };
    
    Optimization: {
        indexes: IndexConfig;
        compression: CompressionConfig;
        performance: PerformanceConfig;
    };
}
```
