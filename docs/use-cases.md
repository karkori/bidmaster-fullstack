# 🎯 Casos de Uso - BidMaster

## 📋 Índice
- [Gestión de Categorías](#gestión-de-categorías)
- [Sistema de Subastas](#sistema-de-subastas)
- [Gestión de Pujas](#gestión-de-pujas)
- [Gestión de Pagos](#gestión-de-pagos)
- [Seguridad y Prevención](#seguridad-y-prevención)
- [Comunicación](#comunicación)
- [Búsqueda y Navegación](#búsqueda-y-navegación)

## 🗂 Gestión de Categorías

### 📝 Descripción
Sistema de organización y clasificación de subastas.

### 🎯 Casos de Uso

#### 1. Gestionar Categorías
- **Actor**: Administrador
- **Flujo Principal**:
  1. Crear categorías y subcategorías
  2. Definir jerarquías
  3. Establecer categorías destacadas
  4. Actualizar o desactivar categorías

#### 2. Navegar Categorías
- **Actor**: Usuario
- **Flujo Principal**:
  1. Ver listado de categorías
  2. Filtrar por categorías destacadas
  3. Explorar subcategorías
  4. Ver número de subastas por categoría

## 🔨 Sistema de Subastas

### 📝 Descripción
Sistema principal de gestión de subastas y sus estados.

### 🎯 Casos de Uso

#### 1. Crear Subasta
- **Actor**: Usuario (Vendedor)
- **Precondiciones**: Usuario autenticado
- **Flujo Principal**:
  1. Subir imágenes del producto
  2. Ingresar título y descripción
  3. Seleccionar categoría:
     - Elegir categoría principal
     - Seleccionar subcategoría si aplica
     - Sistema valida categorización correcta
  4. Establecer precio inicial
  5. Definir duración
  6. Configurar opciones avanzadas:
     - Depósito de garantía
     - Incremento mínimo
     - Opción de pausa

#### 2. Pausar Subasta
- **Actor**: Usuario (Vendedor)
- **Precondiciones**: 
  - Subasta activa
  - Opción de pausa habilitada
- **Flujo Principal**:
  1. Acceder a administración de subasta
  2. Seleccionar "Pausar"
  3. Ingresar motivo obligatorio
  4. Notificar a pujadores
- **Restricciones**:
  - Máximo 1-2 pausas por subasta
  - Duración máxima de pausa: 24h
  - Motivo obligatorio

## 💰 Gestión de Pujas

### 📝 Descripción
Control y gestión del sistema de pujas en tiempo real.

### 🎯 Casos de Uso

#### 1. Realizar Puja
- **Actor**: Usuario (Pujador)
- **Precondiciones**: Subasta activa
- **Flujo Principal**:
  1. Acceder a subasta activa
  2. Ingresar incremento (≥ incremento mínimo)
  3. Validar depósito de garantía:
     - Verificar saldo disponible
     - Bloquear monto requerido
  4. Validar y registrar puja
  5. Emitir evento Kafka
  6. Notificar participantes

#### 2. Abandonar Subasta
- **Actor**: Usuario (Pujador)
- **Precondiciones**: Usuario participante
- **Flujo Principal**:
  1. Solicitar salida
  2. Verificar posición:
     - Si no es pujador más alto: permitir salida
     - Si es pujador más alto: bloquear salida
  3. Gestionar depósito:
     - Devolver si no es ganador
     - Retener si abandona siendo ganador

## 💳 Gestión de Pagos

### 📝 Descripción
Proceso de pago y gestión de transacciones.

### 🎯 Casos de Uso

#### 1. Procesar Pago de Subasta
- **Actor**: Usuario (Ganador)
- **Precondiciones**: Subasta finalizada con ganador
- **Flujo Principal**:
  1. Recibir notificación de victoria
  2. Seleccionar método de pago
  3. Procesar pago:
     - Aplicar depósito bloqueado
     - Completar con método seleccionado
  4. Actualizar estado:
     - Confirmar si exitoso
     - Reintentar si falla

## 🛡️ Seguridad y Prevención

### 📝 Descripción
Sistema de detección y prevención de fraudes.

### 🎯 Casos de Uso

#### 1. Detectar Subastas Fraudulentas
- **Actor**: Sistema
- **Mecanismos**:
  - Análisis de historial de usuario
  - Sistema de reportes comunitarios
  - IA para detección de texto fraudulento
  - Moderación automática preventiva

## 💬 Comunicación

### 📝 Descripción
Sistema de notificaciones y comunicación entre usuarios.

### 🎯 Casos de Uso

#### 1. Gestionar Notificaciones
- **Actor**: Sistema
- **Eventos**:
  - Nueva puja
  - Superación de puja
  - Finalización de subasta
  - Pausas/reactivaciones
  - Confirmaciones de pago

#### 2. Chat Comprador-Vendedor
- **Actor**: Usuario
- **Funcionalidades**:
  - Consultas sobre producto
  - Negociación de términos
  - Coordinación de entrega

## 🔍 Búsqueda y Navegación

### 📝 Descripción
Sistema de búsqueda y navegación principal de la plataforma.

### 🎯 Casos de Uso

#### 1. Mostrar Página Principal
- **Actor**: Usuario
- **Flujo Principal**:
  1. Mostrar categorías destacadas en el header
  2. Listar subastas populares
  3. Mostrar categorías principales con:
     - Imagen representativa
     - Número de subastas activas
     - Subcategorías principales
  4. Mostrar subastas por finalizar pronto

#### 2. Buscar Subastas
- **Actor**: Usuario
- **Flujo Principal**:
  1. Ingresar término de búsqueda
  2. Aplicar filtros:
     - Por categoría
     - Por rango de precio
     - Por estado (activa, finalizada, etc.)
     - Por ubicación
  3. Ordenar resultados:
     - Por relevancia
     - Por precio
     - Por tiempo restante
     - Por número de pujas
  4. Ver resultados en tiempo real
  5. Guardar búsqueda para notificaciones

#### 3. Explorar por Categoría
- **Actor**: Usuario
- **Flujo Principal**:
  1. Seleccionar categoría
  2. Ver subastas de la categoría:
     - Destacadas primero
     - Filtradas por subcategoría
     - Ordenadas por relevancia
  3. Aplicar filtros específicos de la categoría
  4. Ver estadísticas de la categoría:
     - Precio promedio
     - Número de subastas activas
     - Subastas más populares

---

## 📊 Resumen de Casos de Uso

| ID | Caso de Uso | Actor Principal |
|----|-------------|----------------|
| 1 | Registro de Usuario | Usuario |
| 2 | Gestión de Categorías | Administrador |
| 3 | Mostrar Página Principal | Sistema |
| 4 | Buscar Subastas | Usuario |
| 5 | Explorar por Categoría | Usuario |
| 6 | Publicación de Subasta | Vendedor |
| 7 | Pujar en Subasta | Comprador |
| 8 | Finalización de Subasta | Sistema |
| 9 | Pausar Subasta | Vendedor |
| 10 | Abandonar Subasta | Pujador |
| 11 | Procesar Pago | Comprador |
| 12 | Detectar Fraudes | Sistema |
| 13 | Chat | Usuario |
| 14 | Notificaciones | Sistema |

---

> 📌 **Nota**: Este documento está en constante evolución y se actualizará según avance el desarrollo del proyecto.
