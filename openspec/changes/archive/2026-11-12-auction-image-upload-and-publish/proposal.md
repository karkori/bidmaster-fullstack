# Proposal

## Why

Actualmente un vendedor no puede completar el ciclo básico de alta y publicación real de un producto en BidMaster: el selector de imágenes de `CreateAuctionComponent` es estático, el formulario siempre deja las subastas en estado `DRAFT`, omite campos reales del backend (`depositRequired`, `allowPause`) y redirige a `/dashboard` sin enlace al producto; además, las tarjetas y el detalle muestran inconsistencias visuales (`COMPLETED` en lugar de `FINISHED`, fallo al no tener imagen marcada como `primary` o placeholder inexistente). Este primer incremento acotado resuelve la creación, subida segura de imágenes, publicación opcional inmediata y visualización correcta de extremo a extremo.

## What Changes

- **Backend (`bidmaster-ws`) — Subida de imágenes de subasta con metadatos y resolución de usuario:**
  - Extender `ImageUseCase`, `ImageService` e `ImageController` (`POST /api/images`) para aceptar parámetros opcionales `isPrimary` (por defecto `false`) y `displayOrder` (por defecto `0`), garantizando una única imagen principal cuando `isPrimary=true` para la misma subasta.
  - Permitir resolver el usuario propietario desde `Principal` (JWT) cuando el parámetro `userId` se omite en `POST /api/images`, manteniendo compatibilidad con clientes/tests existentes que envían `userId` explícito, y usar fallback `/api/images/{id}/raw` cuando el almacenamiento no devuelva URL pública directa o sea local.
- **Frontend (`bidmaster-front`) — `ImageService` y flujo completo en `CreateAuctionComponent`:**
  - Crear `ImageService` (`src/app/core/services/image.service.ts`) para subir imágenes vía `multipart/form-data`, consultar imágenes por referencia y construir URLs válidas (`/api/images/{id}/raw`).
  - Conectar el selector de archivos y drag-and-drop en `CreateAuctionComponent` con validación en cliente (PNG, JPG, WEBP, GIF ≤ 5 MB), previsualización de miniaturas, eliminación antes de enviar, reordenación básica / selección de imagen principal y subida secuencial/encadenada tras crear la subasta.
  - Alinear el formulario y `CreateAuctionDto` con `CreateAuctionRequestDTO`: incorporar controles para `depositRequired` y `allowPause`, retirar `reservePrice` del formulario (no soportado aún en dominio/BD `Auction`), añadir validación alineada con Zod/Reactive Forms y ofrecer dos acciones reales: **Guardar borrador (`DRAFT`)** y **Crear y publicar (`ACTIVE`)** llamando a `POST /api/auctions/{id}/publish`, redirigiendo al detalle `/auctions/:slugOrId`.
- **Frontend (`bidmaster-front`) — Visualización en tarjetas y galería básica en detalle:**
  - Corregir `AuctionCardComponent` para mapear los estados reales del dominio (`ACTIVE`, `DRAFT`, `PAUSED`, `FINISHED`, `CANCELLED`), usar fallback a la primera imagen disponible (`images[0].url`) si ninguna tiene `primary=true` y reemplazar la ruta rota `assets/images/no-image.png` por un placeholder SVG/asset válido servido desde `public/`.
  - Actualizar `AuctionDetailComponent` para reconocer el estado `FINISHED` (junto a `PAUSED` y `CANCELLED`) y permitir cambiar la imagen principal mostrada haciendo clic en las miniaturas cuando la subasta tenga varias imágenes.

## Capabilities

### New Capabilities
- `auction-image-management`: Subida segura, validación binaria (magic bytes), asignación de imagen principal (`isPrimary`) y orden de visualización (`displayOrder`), resolución del usuario autenticado y recuperación de imágenes de subasta entre `ImageController` y `ImageService`.
- `auction-creation-and-listing`: Flujo completo de creación de producto en subasta alineado con el contrato del dominio `Auction` (`DRAFT` vs `ACTIVE` mediante publicación), validación en frontend, navegación al detalle (`/auctions/:idOrSlug`), renderizado correcto de estados (`AuctionStatus`) e imágenes en tarjetas y galería básica de detalle.

### Modified Capabilities

_(Ninguna; el proyecto aún no cuenta con especificaciones consolidadas en `openspec/specs/`.)_

## Out of Scope

Quedan explícitamente fuera de este incremento (se abordarán en SDDs posteriores):
- Pantalla de gestión del vendedor **"Mis Subastas"** (`/dashboard/my-auctions`) con edición de borradores, pausar/reanudar/cancelar o gestión posterior de imágenes.
- Suscripción SSE en tiempo real e historial completo de pujas (`GET /api/auctions/{id}/bids`) dentro de `AuctionDetailComponent`.
- Conexión de pantallas mock del dashboard (`MyBidsComponent`, `FavoritesComponent`, `NotificationsComponent`, `MessagesComponent`) con datos reales y navegación.
- Soporte en dominio/BD para `reservePrice` (requeriría migración Flyway `V5`).
- Endurecimiento de `SecurityConfig` (retirar `.requestMatchers("/api/**").permitAll()`).

## Impact

- **Modelo de dominio y reglas (`docs/architecture/domain-model.md`):**
  - Entidad `Auction` (`Auction.create`, `Auction.publish`, invariantes de `initialPrice > 0`, `minBidIncrement > 0`, `depositRequired >= 0`, transición `DRAFT -> ACTIVE` y emisión de `AuctionStatusChangedEvent`).
  - Entidad `Image` (`Image.create`, `setAsPrimary()`, `unsetPrimary()`, `setDisplayOrder()`). No requiere cambios de esquema SQL porque la tabla `images` (migración `V4`) ya incluye `is_primary` y `display_order`.
- **Backend (`bidmaster-ws`):** `ImageUseCase`, `ImageService`, `ImageController`, `ImageResponseDTO` (opcionalmente exponer `primary`/`displayOrder`), tests unitarios de servicio/dominio y ampliación de `src/test/resources/karate/images-security.feature`.
- **Frontend (`bidmaster-front`):** Nuevo `ImageService`, `CreateAuctionComponent` (`.ts`/`.html`), `auction.model.ts`, nuevo esquema Zod `auction.schemas.ts`, `AuctionCardComponent` (`.ts`/`.html`), `AuctionDetailComponent` (`.ts`/`.html`), asset placeholder en `public/` y tests unitarios (`*.spec.ts`).
