# 📦 Entidades del Dominio

## 🔍 Core Entities

### 👤 Usuario (User)
[Ver detalles](user.md)
- Gestión de usuarios y roles
- Información personal y autenticación
- Balance y reputación
- Validaciones específicas para la gestión de usuarios y roles
- Métricas de reputación y cómo se calculan

### 📦 Producto (Product)
[Ver detalles](product.md)
- Información del producto
- Imágenes y categorización
- Estados del producto
- Validaciones de información del producto y gestión de imágenes
- Estados del producto y sus transiciones

### 🔨 Subasta (Auction)
[Ver detalles](auction.md)
- Configuración de subasta
- Precios y tiempos
- Estados y participantes
- Validaciones de configuración y gestión de precios y tiempos

### 💰 Puja (Bid)
[Ver detalles](bid.md)
- Registro de pujas
- Depósitos y validaciones
- Estados de puja
- Validaciones de depósitos y gestión de estados de puja

## 📑 Supporting Entities

### 🗂 Categoría (Category)
[Ver detalles](category.md)
- Jerarquía de categorías
- Configuración y visualización
- Relaciones con productos
- Validaciones de jerarquía y gestión de relaciones con productos

### 💳 Pago (Payment)
[Ver detalles](payment.md)
- Procesamiento de pagos
- Estados y confirmaciones
- Métodos de pago
- Validaciones de procesamiento y gestión de estados y confirmaciones

### 🔔 Notificación (Notification)
[Ver detalles](notification.md)
- Tipos de notificaciones
- Entrega y estados
- Preferencias de usuario
- Tipos de notificaciones y gestión de preferencias de usuario

## ⚙️ System Entities

### 🚫 Reporte (Report)
[Ver detalles](report.md)
- Gestión de reportes
- Asignación y seguimiento
- Resolución de problemas
- Validaciones y gestión de reportes

### 📝 Registro de Auditoría (AuditLog)
[Ver detalles](audit.md)
- Registro de cambios
- Trazabilidad
- Seguridad
- Validaciones de trazabilidad y gestión de registros de cambios

### ⚙️ Configuración (SystemConfig)
[Ver detalles](config.md)
- Parámetros del sistema
- Gestión de entornos
- Valores por defecto

### 🔒 Permiso (Permission)
[Ver detalles](permission.md)
- Control de acceso
- Roles y recursos
- Matriz de permisos
- Validaciones de control de acceso y gestión de roles y recursos

## 🔄 Relaciones entre Entidades

[Ver diagrama de relaciones](relationships.md)

## 📊 Value Objects

[Ver lista de Value Objects](value-objects.md)
