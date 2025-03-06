# 🔔 Reglas de Notificaciones

## 📝 Reglas Generales

### Tipos de Notificación
1. Sistema
   - Mantenimiento programado
   - Actualizaciones
   - Cambios de política
   - Alertas de seguridad

2. Transaccional
   - Confirmaciones de pago
   - Estados de envío
   - Actualizaciones de subasta
   - Mensajes de soporte

3. Marketing
   - Promociones
   - Recomendaciones
   - Newsletters
   - Eventos especiales

### Prioridades
1. Crítica (inmediata)
   - Seguridad
   - Pagos
   - Pujas ganadoras
   - Problemas técnicos

2. Alta (< 5 min)
   - Fin de subasta
   - Superado en puja
   - Mensaje nuevo
   - Disputa

3. Normal (< 1 hora)
   - Actualizaciones
   - Recordatorios
   - Recomendaciones
   - Newsletter

## 🛡️ Configuración

### Preferencias Usuario
```typescript
interface NotificationPreferences {
    channels: {
        email: boolean;
        push: boolean;
        sms: boolean;
        inApp: boolean;
    };
    
    types: {
        system: boolean;
        transaction: boolean;
        marketing: boolean;
        social: boolean;
    };
    
    frequency: {
        instant: string[];
        daily: string[];
        weekly: string[];
        disabled: string[];
    };
}
```

### Canales
```typescript
interface NotificationChannels {
    email: {
        priority: 1-3;
        templates: string[];
        maxDaily: number;
    };
    
    push: {
        priority: 1-3;
        templates: string[];
        maxHourly: number;
    };
    
    sms: {
        priority: 1;
        templates: string[];
        maxDaily: number;
    };
}
```

## 🎯 Entrega y Seguimiento

### Reglas de Entrega
1. Multi-canal
   - Prioridad por tipo
   - Fallback automático
   - Confirmación de entrega
   - Registro de intentos

2. Agrupación
   - Máximo 3 similares
   - Intervalo mínimo
   - Smart bundling
   - Prioridad individual

### Tracking
```typescript
interface NotificationTracking {
    sent: timestamp;
    delivered: timestamp;
    read: timestamp;
    clicked: timestamp;
    action: string;
    
    calculateEngagement(): number {
        // Fórmula de engagement basada en interacción
    }
}
```

## 📊 Límites y Throttling

### Por Usuario
```typescript
interface UserLimits {
    maxDaily = {
        email: 10,
        push: 20,
        sms: 5,
        total: 30
    };
    
    maxHourly = {
        push: 5,
        marketing: 2,
        total: 10
    };
}
```

### Por Sistema
```typescript
interface SystemLimits {
    maxBatchSize = 1000;
    maxRetries = 3;
    rateLimitPerSecond = 100;
    cooldownPeriod = 300; // segundos
}
```

## 🔄 Plantillas y Personalización

### Componentes
1. Contenido
   - Título dinámico
   - Cuerpo personalizado
   - Variables contextuales
   - Links trackables

2. Estilo
   - Tema por tipo
   - Branding consistente
   - Responsive design
   - Accesibilidad

### Personalización
```typescript
interface NotificationTemplate {
    base: string;
    variables: string[];
    conditions: Record<string, any>;
    variations: string[];
    
    render(context: any): string {
        // Lógica de renderizado personalizado
    }
}
```

## 🚫 Restricciones

### Horarios
1. Marketing
   - No antes de 9:00
   - No después de 21:00
   - Respeta zona horaria
   - Días laborables

2. Transaccional
   - 24/7 permitido
   - Prioridad respetada
   - Agrupación inteligente
   - Urgencia considerada

### Contenido
1. Longitud máxima
   - Push: 140 caracteres
   - SMS: 160 caracteres
   - Email: 2000 caracteres
   - In-app: 500 caracteres

2. Formato
   - HTML solo en email
   - Emojis limitados
   - Links seguros
   - Media optimizada

## 📱 Gestión de Preferencias

### Control Usuario
1. Suscripción
   - Opt-in requerido
   - Granular por tipo
   - Fácil gestión
   - Preferencias guardadas

2. Frecuencia
   - Instantánea
   - Resumen diario
   - Resumen semanal
   - Silencio temporal

## 📊 Analytics

### Métricas
1. Entrega
   - Tasa de entrega
   - Tiempo promedio
   - Fallos
   - Rebotes

2. Engagement
   - Tasa de apertura
   - Clics
   - Acciones
   - Conversiones

### Reportes
1. Tiempo Real
   - Queue status
   - Delivery rate
   - Error rate
   - Performance

2. Agregados
   - Tendencias
   - Patrones
   - Efectividad
   - ROI

## 🔄 Automatización

### Triggers
1. Eventos
   - Usuario
   - Sistema
   - Tiempo
   - Condición

2. Acciones
   - Envío
   - Actualización
   - Cancelación
   - Reintento

### Workflows
```typescript
interface NotificationWorkflow {
    trigger: TriggerType;
    conditions: Condition[];
    actions: Action[];
    fallbacks: Action[];
    
    execute(context: any): Promise<void> {
        // Lógica de ejecución del workflow
    }
}
```
