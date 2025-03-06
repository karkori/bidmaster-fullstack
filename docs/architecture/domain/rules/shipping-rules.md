# 📦 Reglas de Envíos

## 📝 Reglas Generales

### Configuración de Envío
1. Dirección verificada obligatoria
2. Tiempo máximo de preparación: 3 días
3. Tracking obligatorio para items > $50
4. Seguro obligatorio para items > $500
5. Restricciones por país/región

### Costos y Tarifas
1. Basado en peso y dimensiones
2. Tarifas por zona geográfica
3. Recargos por manejo especial
4. Seguro opcional (1-5% del valor)
5. Descuentos por volumen

### Tiempos de Entrega
1. Estimación basada en destino
2. Ajuste por tipo de servicio
3. Consideración de días festivos
4. Ventanas de entrega específicas
5. Garantías de tiempo según servicio

## 🛡️ Validaciones

### Pre-Envío
```typescript
interface PreShippingChecks {
    validateAddress(): boolean;
    checkRestrictions(): boolean;
    validateDimensions(): boolean;
    verifyInsuranceRequirements(): boolean;
    calculateCosts(): ShippingCosts;
}
```

### Durante Envío
```typescript
interface ActiveShippingChecks {
    validateTracking(): boolean;
    monitorProgress(): boolean;
    checkDeliveryWindow(): boolean;
    verifyHandling(): boolean;
}
```

### Post-Envío
```typescript
interface PostShippingChecks {
    confirmDelivery(): boolean;
    validateCondition(): boolean;
    processReturn(): boolean;
    calculateRefund(): boolean;
}
```

## 🎯 Gestión de Envíos

### Preparación
1. Empaquetado según producto
2. Etiquetado correcto
3. Documentación requerida
4. Verificación de restricciones
5. Selección de servicio

### Seguimiento
1. Actualización en tiempo real
2. Notificaciones de estado
3. Gestión de incidencias
4. Prueba de entrega
5. Registro de eventos

### Devoluciones
1. Plazo máximo: 14 días
2. Condiciones específicas
3. Costos de devolución
4. Proceso de inspección
5. Reembolso según estado

## 📊 Cálculos y Fórmulas

### Costos de Envío
```typescript
interface ShippingCosts {
    baseCost = weight * zoneRate;
    dimensionalWeight = (length * width * height) / divisor;
    handlingFee = baseHandling * complexity;
    insurance = declaredValue * insuranceRate;
    totalCost = max(baseCost, dimensionalWeight) + handlingFee + insurance;
}
```

### Tiempos Estimados
```typescript
interface DeliveryEstimates {
    baseTime = zoneDistance * serviceSpeed;
    processingTime = 1 business day;
    customsTime = international ? 2-5 days : 0;
    totalTime = baseTime + processingTime + customsTime;
}
```

## 🔄 Estados de Envío

### Flujo Principal
```
[Pendiente] → [Preparación] → [En Tránsito] → [Entregado]
     ↓             ↓              ↓              ↓
[Cancelado]    [Retrasado]    [Perdido]    [Devuelto]
```

### Eventos de Tracking
1. Etiqueta creada
2. Recogido
3. En centro de distribución
4. En ruta
5. Entregado

## 🚫 Restricciones

### Productos
1. Dimensiones máximas
   - Largo: 150cm
   - Ancho: 100cm
   - Alto: 100cm
   - Peso: 30kg

2. Artículos prohibidos
   - Materiales peligrosos
   - Productos perecederos
   - Items restringidos
   - Mercancías ilegales

### Destinos
1. Cobertura geográfica
   - Países servidos
   - Zonas restringidas
   - Áreas remotas
   - Tiempos especiales

2. Restricciones
   - Aduanas
   - Documentación
   - Permisos
   - Certificaciones

## 📱 Notificaciones

### Al Vendedor
1. Orden lista para envío
2. Recordatorio de preparación
3. Etiqueta generada
4. Recogida programada
5. Incidencias

### Al Comprador
1. Envío confirmado
2. Tracking disponible
3. Actualizaciones de estado
4. Entrega programada
5. Confirmación de entrega

### Alertas
1. Retrasos
2. Incidencias
3. Intentos de entrega
4. Devoluciones
5. Reclamaciones

## 🌍 Servicios por Región

### Nacional
1. Estándar (3-5 días)
2. Expreso (1-2 días)
3. Same-day (ciudades principales)
4. Punto de recogida

### Internacional
1. Económico (7-15 días)
2. Estándar (5-7 días)
3. Expreso (3-4 días)
4. Priority (1-2 días)
