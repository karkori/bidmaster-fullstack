# 🏗 Modelo de Dominio - BidMaster

## 📊 Entidades Principales

### 👤 Usuario (User)
- **Atributos**:
  - ID
  - Email
  - Nombre
  - Contraseña (hash)
  - Fecha de registro
  - Estado (activo, bloqueado)
  - Rol (usuario, administrador)
  - Balance disponible
  - Reputación

### 📦 Producto (Product)
- **Atributos**:
  - ID
  - Título
  - Descripción
  - Imágenes
  - Estado (borrador, publicado, vendido)
  - Categoría
  - Vendedor (Usuario)
  - Ubicación

### 🔨 Subasta (Auction)
- **Atributos**:
  - ID
  - Producto
  - Precio inicial
  - Precio actual
  - Incremento mínimo
  - Fecha inicio
  - Fecha fin
  - Estado (activa, pausada, finalizada)
  - Depósito requerido (%)
  - Vendedor
  - Comprador (ganador)
  - Permite pausa
  - Número de pausas usadas

### 💰 Puja (Bid)
- **Atributos**:
  - ID
  - Subasta
  - Pujador
  - Cantidad
  - Fecha
  - Estado (activa, superada, ganadora)
  - Depósito bloqueado

### 🗂 Categoría (Category)
- **Atributos**:
  - ID
  - Nombre
  - Slug
  - Descripción
  - Imagen
  - Categoría padre
  - Destacada
  - Orden de visualización

### 💳 Pago (Payment)
- **Atributos**:
  - ID
  - Subasta
  - Comprador
  - Vendedor
  - Monto
  - Estado
  - Método de pago
  - Depósito aplicado
  - Fecha

### 🔔 Notificación (Notification)
- **Atributos**:
  - ID
  - Usuario
  - Tipo
  - Contenido
  - Estado (leída, no leída)
  - Fecha
  - Enlace

### 🚫 Reporte (Report)
- **Atributos**:
  - ID
  - Tipo (subasta, usuario, comentario)
  - Entidad reportada
  - Usuario reportador
  - Motivo
  - Descripción
  - Estado (pendiente, revisado, cerrado)
  - Fecha
  - Administrador asignado
  - Resolución

### 📝 Registro de Auditoría (AuditLog)
- **Atributos**:
  - ID
  - Fecha
  - Usuario
  - Tipo de acción
  - Entidad afectada
  - Detalles del cambio
  - IP
  - User Agent

### ⚙️ Configuración del Sistema (SystemConfig)
- **Atributos**:
  - Clave
  - Valor
  - Tipo (número, texto, booleano)
  - Descripción
  - Última modificación
  - Modificado por
  - Entorno (desarrollo, producción)

### 🔒 Permiso (Permission)
- **Atributos**:
  - ID
  - Nombre
  - Descripción
  - Recurso
  - Acciones permitidas
  - Roles asociados

## 🔄 Relaciones

### Usuario
- Puede tener múltiples productos en venta
- Puede tener múltiples pujas activas
- Puede tener múltiples notificaciones
- Puede tener múltiples pagos (como comprador o vendedor)

### Producto
- Pertenece a un usuario (vendedor)
- Pertenece a una categoría
- Tiene una subasta asociada

### Subasta
- Tiene un producto
- Tiene un vendedor
- Puede tener un comprador (ganador)
- Tiene múltiples pujas
- Puede tener un pago asociado

### Puja
- Pertenece a una subasta
- Tiene un pujador (usuario)
- Puede tener un depósito asociado

### Categoría
- Puede tener una categoría padre
- Puede tener múltiples subcategorías
- Tiene múltiples productos

### Pago
- Asociado a una subasta
- Tiene un comprador
- Tiene un vendedor

### Reporte
- Asociado a una entidad reportada
- Creado por un usuario
- Puede ser asignado a un administrador
- Genera entradas en el registro de auditoría

### Registro de Auditoría
- Asociado a un usuario
- Puede estar relacionado con cualquier entidad
- Mantiene historial de cambios de configuración

### Permiso
- Asociado a roles de usuario
- Define acceso a recursos del sistema

## 🎯 Reglas de Negocio

### Subastas
1. Una subasta debe tener un precio inicial > 0
2. El incremento mínimo debe ser > 0
3. La fecha fin debe ser posterior a la fecha inicio
4. Solo el vendedor puede pausar si está permitido
5. Máximo 2 pausas por subasta
6. Duración máxima de pausa: 24h

### Pujas
1. La cantidad debe ser ≥ precio actual + incremento mínimo
2. Si hay depósito requerido, debe bloquearse antes de pujar
3. No se puede pujar en subastas propias
4. No se puede pujar en subastas finalizadas o pausadas

### Pagos
1. El monto debe coincidir con la puja ganadora
2. El depósito bloqueado se aplica al pago final
3. Si el ganador no paga, pierde el depósito

### Categorías
1. Una categoría puede ser destacada
2. Una categoría puede tener subcategorías
3. Un producto solo puede estar en una categoría

### Administración
1. Solo administradores pueden gestionar categorías destacadas
2. Los reportes deben ser atendidos en orden de llegada
3. Cambios críticos requieren aprobación de múltiples administradores
4. Todas las acciones administrativas deben ser registradas
5. Los administradores no pueden participar en subastas

### Sistema
1. Mantener registro de auditoría de cambios críticos
2. Backup automático de datos críticos
3. Limpieza periódica de notificaciones antiguas
4. Archivado de subastas finalizadas después de X días
5. Límite de intentos fallidos de login
6. Bloqueo automático de cuentas sospechosas
7. Validación de direcciones IP y geolocalización

## 🔒 Invariantes del Sistema

1. El balance de un usuario no puede ser negativo
2. Una subasta no puede tener pujas menores a las anteriores
3. Un usuario no puede pujar si está bloqueado
4. El depósito bloqueado no puede exceder el balance disponible
5. Una subasta finalizada no puede modificarse
6. Los permisos de administrador no pueden modificarse por el propio usuario
7. Todo cambio en la configuración del sistema debe registrarse
8. Los reportes no pueden eliminarse, solo cerrarse
9. Las acciones de auditoría son inmutables
10. La configuración del sistema debe mantener consistencia entre entornos

## 📈 Estados y Transiciones

### Estados de Subasta
```
[Borrador] -> [Activa] -> [Finalizada]
                ↑   ↓
              [Pausada]
```

### Estados de Puja
```
[Nueva] -> [Activa] -> [Superada/Ganadora]
```

### Estados de Pago
```
[Pendiente] -> [Procesando] -> [Completado/Fallido]
```

### Estados de Reporte
```
[Nuevo] -> [En Revisión] -> [Resuelto]
              ↓
         [Escalado] -> [Cerrado]
```

### Estados de Usuario
```
[Activo] <-> [Bloqueado]
   ↓
[Suspendido] -> [Baneado]
