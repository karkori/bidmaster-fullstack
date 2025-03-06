# 🔨 Reglas de Subastas

## 📝 Reglas Generales

### Creación de Subastas
1. Solo usuarios verificados pueden crear subastas
2. El producto debe estar completamente descrito
3. Debe tener al menos una imagen
4. El precio inicial debe ser mayor que 0
5. La fecha de inicio debe ser futura
6. La duración debe estar entre 1 y 30 días

### Precio y Pujas
1. El incremento mínimo debe ser al menos 1% del precio actual
2. El depósito requerido debe estar entre 5% y 20%
3. No se permiten pujas en los últimos 5 segundos (extensión automática)
4. El precio de reserva es opcional pero debe ser ≥ precio inicial
5. Las pujas automáticas deben respetar los mismos límites

### Participación
1. Un vendedor no puede pujar en su propia subasta
2. Un usuario bloqueado no puede participar
3. Se requiere balance suficiente para el depósito
4. Máximo 50 pujas por usuario en una subasta
5. No se permiten pujas después de la fecha de fin

## 🛡️ Validaciones

### Pre-Subasta
```typescript
interface PreAuctionChecks {
    validateProduct(): boolean;
    validatePricing(): boolean;
    validateDates(): boolean;
    validateSeller(): boolean;
}
```

### Durante Subasta
```typescript
interface ActiveAuctionChecks {
    validateBid(bid: Bid): boolean;
    checkTimeExtension(): boolean;
    verifyUserEligibility(user: User): boolean;
    validatePauseRequest(): boolean;
}
```

### Post-Subasta
```typescript
interface PostAuctionChecks {
    validateWinner(): boolean;
    verifyPaymentEligibility(): boolean;
    checkShippingRequirements(): boolean;
}
```

## 🎯 Comportamiento Específico

### Extensión Automática
1. Si hay una puja en los últimos 2 minutos
2. Se extiende 2 minutos desde la última puja
3. Máximo 5 extensiones por subasta
4. Se notifica a todos los participantes

### Pausas
1. Máximo 2 pausas por subasta
2. Duración máxima de 24 horas
3. No permitido en las últimas 12 horas
4. Requiere justificación válida

### Cancelación
1. Solo permitida si no hay pujas
2. Penalización si es injustificada
3. Requiere aprobación si hay pujas
4. Se devuelven todos los depósitos

## 📊 Cálculos y Fórmulas

### Depósitos
```typescript
interface DepositCalculations {
    minDeposit = currentPrice * 0.05;
    maxDeposit = currentPrice * 0.20;
    requiredDeposit = max(minDeposit, customDeposit);
}
```

### Incrementos
```typescript
interface BidIncrements {
    minIncrement = currentPrice * 0.01;
    suggestedIncrements = [
        currentPrice * 0.01,
        currentPrice * 0.02,
        currentPrice * 0.05
    ];
}
```

## 🔄 Estados y Transiciones

### Estados Permitidos
```
[Borrador] → [Programada] → [Activa] → [Finalizada]
                ↑   ↓           ↓
              [Pausada]     [Cancelada]
```

### Condiciones de Transición
1. Borrador → Programada: Validación completa
2. Programada → Activa: Fecha inicio alcanzada
3. Activa → Pausada: Solicitud aprobada
4. Activa → Finalizada: Tiempo completado
5. Activa → Cancelada: Condiciones cumplidas

## 🚫 Restricciones

### Productos
1. No permitidos:
   - Artículos ilegales
   - Productos falsificados
   - Artículos peligrosos
   - Servicios personales

### Usuarios
1. Límites por usuario:
   - Máximo 10 subastas activas
   - Máximo 50 pujas activas
   - Mínimo 90% rating positivo

### Tiempos
1. Restricciones temporales:
   - Mínimo 1 día
   - Máximo 30 días
   - No iniciar en festivos
   - Horario comercial preferido

## 📱 Notificaciones

### Eventos Notificables
1. Inicio de subasta
2. Nueva puja
3. Superado en puja
4. Próximo a finalizar
5. Finalización
6. Ganador
7. Cambios de estado

### Canales
1. Prioridad por tipo:
   - Críticas: Email + Push + SMS
   - Importantes: Email + Push
   - Informativas: Push
