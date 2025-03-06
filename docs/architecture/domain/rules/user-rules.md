# 👤 Reglas de Usuarios

## 📝 Reglas Generales

### Registro y Verificación
1. Email único y verificado
2. Contraseña segura (8+ caracteres, mayúsculas, números)
3. Nombre de usuario único y apropiado
4. Verificación en dos pasos para acciones críticas
5. Datos personales completos para participar

### Balance y Depósitos
1. Balance no puede ser negativo
2. Depósitos bloqueados no exceden balance disponible
3. Retiros requieren verificación adicional
4. Límites diarios/mensuales de transacciones
5. Periodo de retención para nuevos fondos

### Reputación
1. Score basado en últimas 100 transacciones
2. Penalizaciones por cancelaciones injustificadas
3. Bonificaciones por transacciones exitosas
4. Niveles de usuario por reputación
5. Restricciones basadas en score

## 🛡️ Validaciones

### Registro
```typescript
interface RegistrationChecks {
    validateEmail(): boolean;
    validatePassword(): boolean;
    validateUsername(): boolean;
    checkAgeRequirement(): boolean;
    verifyLocation(): boolean;
}
```

### Transacciones
```typescript
interface TransactionChecks {
    validateBalance(amount: Decimal): boolean;
    checkDailyLimit(amount: Decimal): boolean;
    verifyWithdrawalEligibility(): boolean;
    validateDepositSource(): boolean;
}
```

### Seguridad
```typescript
interface SecurityChecks {
    validateLoginAttempt(): boolean;
    requiresTwoFactor(): boolean;
    checkDeviceFingerprint(): boolean;
    validateSessionActivity(): boolean;
}
```

## 🎯 Comportamiento Específico

### Sistema de Niveles
1. Novato (0-10 transacciones)
   - Límites reducidos
   - Verificación extra
   - Sin subastas premium

2. Regular (11-50 transacciones)
   - Límites estándar
   - Acceso a subastas normales
   - Retiros permitidos

3. Experto (51+ transacciones)
   - Límites aumentados
   - Subastas premium
   - Retiros prioritarios

4. VIP (Invitación)
   - Sin límites
   - Subastas exclusivas
   - Soporte dedicado

### Penalizaciones
1. Advertencia
   - Primera falta leve
   - 24h de restricción
   - Notificación

2. Suspensión Temporal
   - Faltas repetidas
   - 7-30 días
   - Revisión manual

3. Suspensión Permanente
   - Faltas graves
   - Cuenta bloqueada
   - Sin apelación

## 📊 Cálculos y Fórmulas

### Score de Reputación
```typescript
interface ReputationScore {
    baseScore = positiveRatings / totalRatings * 100;
    weightedScore = baseScore * transactionVolumeFactor;
    penaltyFactor = 1 - (violations * 0.1);
    finalScore = weightedScore * penaltyFactor;
}
```

### Límites Financieros
```typescript
interface FinancialLimits {
    dailyLimit = baseLimit * userLevel;
    withdrawalLimit = availableBalance * 0.8;
    depositLimit = baseLimit * 2;
    instantWithdrawal = reputation > 95;
}
```

## 🔄 Estados de Usuario

### Estados Principales
```
[Pendiente] → [Activo] → [Verificado]
     ↓           ↓
[Rechazado]  [Suspendido] → [Baneado]
```

### Condiciones de Cambio
1. Pendiente → Activo: Email verificado
2. Activo → Verificado: KYC completo
3. Activo → Suspendido: Violaciones
4. Suspendido → Baneado: Faltas graves

## 🚫 Restricciones

### Acceso
1. Geográficas:
   - Países permitidos
   - Restricciones locales
   - IPs bloqueadas

2. Temporales:
   - Horarios de trading
   - Períodos de mantenimiento
   - Cooling-off periods

### Operativas
1. Por nivel:
   - Límites de subastas
   - Límites de pujas
   - Acceso a funciones

2. Por reputación:
   - Productos permitidos
   - Montos máximos
   - Requisitos de depósito

## 📱 Notificaciones

### Seguridad
1. Críticas:
   - Inicio de sesión nuevo dispositivo
   - Cambio de contraseña
   - Retiro de fondos
   - Cambio de email

2. Importantes:
   - Depósito recibido
   - Nivel actualizado
   - Verificación completada
   - Límites modificados

3. Informativas:
   - Nuevas características
   - Tips de seguridad
   - Recordatorios
   - Promociones
