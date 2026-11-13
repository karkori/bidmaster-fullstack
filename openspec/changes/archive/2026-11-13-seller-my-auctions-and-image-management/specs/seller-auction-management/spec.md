# Spec Delta

## Purpose

Permitir al vendedor autenticado consultar, filtrar y gestionar el ciclo de vida completo de sus subastas desde el dashboard, incluyendo la edición integral de borradores (`DRAFT`) y sus imágenes asociadas.

## ADDED Requirements

### Requirement: Listado y filtrado de subastas propias del vendedor en el dashboard

El frontend SHALL exponer la vista protegida `/dashboard/my-auctions`, accesible desde el menú lateral y de usuario, para listar las subastas del vendedor autenticado (`GET /api/auctions?sellerId=<userId>`) y filtrarlas por estado y título.

#### Scenario: Carga inicial de subastas propias compatible con SSR
- **GIVEN** un vendedor autenticado con sesión activa en navegador que accede a `/dashboard/my-auctions`
- **WHEN** se inicializa la vista (respetando `isPlatformBrowser` en SSR)
- **THEN** consulta `GET /api/auctions?sellerId=<sellerId>` y muestra sus subastas con imagen de portada, estado, precio actual, pujas totales y fecha de finalización o tiempo restante

#### Scenario: Filtrado por estado y búsqueda por título
- **GIVEN** un vendedor con subastas en estados `DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED` y `CANCELLED`
- **WHEN** selecciona un filtro de estado (`ALL`, `DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED`, `CANCELLED`) o escribe un término de búsqueda
- **THEN** la vista actualiza reactivamente la lista mostrando solo las subastas coincidentes y presenta un estado vacío descriptivo cuando no hay coincidencias

#### Scenario: Acceso directo desde el menú del dashboard y menú de usuario
- **GIVEN** un usuario autenticado en la aplicación
- **WHEN** abre la barra lateral de `DashboardLayoutComponent` o el menú desplegable de `AuthButtonComponent`
- **THEN** visualiza el enlace **"Mis subastas"** apuntando a `/dashboard/my-auctions`

### Requirement: Operación del ciclo de vida de subastas propias según invariantes de dominio

La vista `/dashboard/my-auctions` SHALL mostrar únicamente acciones válidas según el estado de cada subasta e invariantes de `Auction`, invocando los endpoints del backend y mostrando mensajes RFC 7807 `ProblemDetail` en caso de error.

#### Scenario: Publicar o eliminar una subasta en estado DRAFT
- **GIVEN** una subasta propia en estado `DRAFT` listada en `/dashboard/my-auctions`
- **WHEN** el vendedor pulsa **Publicar** (`POST /api/auctions/{id}/publish`) o confirma **Eliminar borrador** (`DELETE /api/auctions/{id}`)
- **THEN** el backend valida que el solicitante es el propietario (`403 Forbidden` si es otro usuario), aplica la transición `DRAFT -> ACTIVE` o elimina el borrador (`204 No Content`), y la lista actualiza inmediatamente el estado mostrado

#### Scenario: Pausar y reanudar una subasta ACTIVE respetando límite de pausas
- **GIVEN** una subasta propia en estado `ACTIVE` con `allowPause=true`
- **WHEN** el vendedor ejecuta **Pausar** (`POST /api/auctions/{id}/pause`) y posteriormente **Reanudar** (`POST /api/auctions/{id}/resume`)
- **THEN** la subasta transiciona `ACTIVE -> PAUSED -> ACTIVE` extendiendo `endDate` por la duración de la pausa; si `allowPause=false` o ya consumió 2 pausas, el backend responde `400 Bad Request` (`ProblemDetail`) y la UI informa el motivo

#### Scenario: Cancelar una subasta ACTIVE o PAUSED
- **GIVEN** una subasta propia en estado `ACTIVE` o `PAUSED`
- **WHEN** el vendedor confirma **Cancelar** (`POST /api/auctions/{id}/cancel`)
- **THEN** la subasta pasa a estado `CANCELLED`, el backend libera el depósito bloqueado de la puja líder si existía y la tarjeta refleja el estado `Cancelada`

### Requirement: Edición integral de borradores e imágenes asociadas

El sistema MUST permitir editar en `/dashboard/edit-auction/:id` los atributos principales y la galería de imágenes de una subasta propia en estado `DRAFT`, normalizando categorías entre enum y nombre visible y rechazando valores inválidos o estados no editables.

#### Scenario: Carga y actualización exitosa de un borrador existente
- **GIVEN** una subasta propia en estado `DRAFT` cuyo DTO de respuesta incluye `category` con su `displayName` (por ejemplo, `"Electrónica"`)
- **WHEN** el vendedor abre `/dashboard/edit-auction/:id`, modifica campos válidos y pulsa **Guardar cambios** (`PUT /api/auctions/{id}`) o **Guardar y publicar**
- **THEN** el backend acepta la categoría por nombre de enum o `displayName`, actualiza la subasta (`200 OK`), ejecuta cualquier subida, eliminación, reordenación o cambio de portada de imágenes pendiente y, si se solicitó publicar, invoca `POST /api/auctions/{id}/publish`

#### Scenario: Rechazo por categoría inválida, estado no DRAFT o usuario no propietario
- **GIVEN** una petición `PUT /api/auctions/{id}` con categoría inexistente, sobre una subasta en estado `ACTIVE` o enviada por un usuario distinto al vendedor
- **WHEN** el backend procesa la solicitud
- **THEN** responde `400 Bad Request` (`ProblemDetail`) ante categoría inválida o estado distinto de `DRAFT`, o `403 Forbidden` si el usuario no es el vendedor propietario
