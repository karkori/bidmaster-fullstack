# 🔄 Estados y Transiciones

## 📊 Diagramas de Estado

### 🔨 [Estados de Subasta](auction-states.md)
```
[Borrador] -> [Activa] -> [Finalizada]
                ↑   ↓
              [Pausada]
```
- Transiciones permitidas
- Validaciones por estado
- Acciones disponibles
- Eventos generados

### 💰 [Estados de Puja](bid-states.md)
```
[Nueva] -> [Activa] -> [Superada/Ganadora]
```
- Condiciones de transición
- Efectos en el sistema
- Notificaciones generadas
- Validaciones específicas
- Reglas financieras
- Métricas y KPIs

### 💳 [Estados de Pago](payment-states.md)
```
[Pendiente] -> [Procesando] -> [Completado/Fallido]
```
- Flujo de procesamiento
- Validaciones por estado
- Acciones de recuperación
- Notificaciones

### 🚫 [Estados de Reporte](report-states.md)
```
[Nuevo] -> [En Revisión] -> [Resuelto]
              ↓
         [Escalado] -> [Cerrado]
```
- Flujo de moderación
- Asignación de responsables
- Acciones por estado
- Resoluciones posibles
- Reglas de moderación por tipo
- Acciones específicas para cada estado

### 👤 [Estados de Usuario](user-states.md)
```
[Activo] <-> [Bloqueado]
   ↓
[Suspendido] -> [Baneado]
```
- Condiciones de cambio
- Efectos en el sistema
- Procesos de recuperación
- Notificaciones

## 🎯 [Eventos del Sistema](system-events.md)
- Eventos por estado
- Suscriptores
- Acciones automáticas
- Notificaciones generadas
- Estructura de eventos y acciones automáticas

## 📝 [Registro de Estados](state-logging.md)
- Auditoría de cambios
- Histórico de transiciones
- Razones de cambio
- Responsables
- Tipos de logs y su estructura
- Políticas de seguridad y mantenimiento
