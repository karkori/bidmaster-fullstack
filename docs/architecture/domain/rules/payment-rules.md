# 💳 Reglas de Pagos

## 📝 Reglas Generales

### Procesamiento de Pagos
1. Tiempo límite de 48h para pago después de subasta
2. Verificación de fondos antes de aceptar pago
3. Confirmación en múltiples pasos para montos altos
4. Bloqueo temporal de fondos durante procesamiento
5. Registro detallado de cada transacción

### Comisiones y Costos
1. Comisión base del marketplace: 5%
2. Comisión variable por categoría: 2-10%
3. Cargo por procesamiento de pago: 1-3%
4. Costos de seguro opcional: 1-5%
5. Descuentos por volumen de ventas

### Seguridad
1. Verificación 3D Secure para tarjetas
2. Límites por método de pago
3. Detección de patrones fraudulentos
4. Validación de dirección de facturación
5. Monitoreo de transacciones inusuales

## 🛡️ Validaciones

### Pre-Pago
```typescript
interface PrePaymentChecks {
    validateAmount(): boolean;
    checkUserLimits(): boolean;
    verifyPaymentMethod(): boolean;
    validateBillingInfo(): boolean;
    checkFraudScore(): number;
}
```

### Durante Procesamiento
```typescript
interface ProcessingChecks {
    verifyFunds(): boolean;
    checkTransactionLimits(): boolean;
    validateSecurityChecks(): boolean;
    monitorVelocity(): boolean;
    validateCurrency(): boolean;
}
```

### Post-Pago
```typescript
interface PostPaymentChecks {
    confirmSettlement(): boolean;
    validateReconciliation(): boolean;
    checkChargebackRisk(): number;
    verifyNotification(): boolean;
}
```

## 🎯 Procesamiento de Pagos

### Flujo Principal
1. Validación inicial
2. Autorización
3. Captura
4. Confirmación
5. Liquidación

### Reintentos
1. Máximo 3 intentos por pago
2. Espera progresiva entre intentos
3. Notificación después de cada fallo
4. Cancelación automática después del último intento

### Reembolsos
1. Disponibles hasta 30 días después
2. Requieren aprobación para montos altos
3. Solo al método original de pago
4. Comisiones no reembolsables
5. Documentación obligatoria

## 📊 Cálculos y Fórmulas

### Comisiones
```typescript
interface FeeCalculations {
    baseFee = amount * 0.05;
    categoryFee = amount * categoryRate;
    processingFee = amount * processorRate;
    totalFees = baseFee + categoryFee + processingFee;
}
```

### Límites
```typescript
interface TransactionLimits {
    dailyLimit = baseLimit * userLevel;
    singleTransactionLimit = dailyLimit * 0.5;
    monthlyLimit = dailyLimit * 30;
    velocityLimit = transactions per hour;
}
```

## 🔄 Estados de Pago

### Flujo Principal
```
[Pendiente] → [Autorizado] → [Capturado] → [Completado]
     ↓             ↓             ↓            ↓
[Cancelado]    [Expirado]    [Fallido]    [Reembolsado]
```

### Tiempos Límite
1. Pendiente → Autorizado: 15 minutos
2. Autorizado → Capturado: 24 horas
3. Capturado → Completado: 48 horas
4. Disputa → Resolución: 15 días

## 🚫 Restricciones

### Métodos de Pago
1. Tarjetas de Crédito/Débito
   - Visa
   - Mastercard
   - American Express
   - Límites específicos por tipo

2. Transferencias Bancarias
   - SEPA (Europa)
   - ACH (USA)
   - Tiempo de procesamiento
   - Montos mínimos/máximos

3. Monederos Electrónicos
   - PayPal
   - Stripe
   - Límites diarios
   - Países soportados

### Seguridad
1. Verificaciones Requeridas
   - KYC para montos altos
   - 2FA para cambios
   - Verificación de dirección
   - Validación de tarjeta

2. Restricciones
   - IPs bloqueadas
   - Países restringidos
   - Métodos limitados
   - Montos máximos

## 📱 Notificaciones

### Eventos Críticos
1. Pago iniciado
2. Autorización exitosa/fallida
3. Captura completada
4. Pago finalizado
5. Reembolso procesado

### Alertas de Seguridad
1. Intento de pago inusual
2. Múltiples fallos
3. Cambio de método
4. Disputa iniciada

### Recordatorios
1. Pago pendiente
2. Próximo a expirar
3. Documentación requerida
4. Confirmación necesaria
