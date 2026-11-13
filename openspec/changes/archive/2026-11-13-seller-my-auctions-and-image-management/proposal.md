# Proposal

## Why

Aunque el vendedor ya puede crear subastas en borrador (`DRAFT`) o publicarlas con imágenes en `/dashboard/create-auction`, no existe ninguna pantalla para consultar sus propias subastas ni para editar borradores, ejecutar acciones de ciclo de vida (`Publicar`, `Pausar`, `Reanudar`, `Cancelar`, `Eliminar borrador`) o reordenar y cambiar la imagen principal de una subasta ya creada. Completar este flujo en el dashboard y exponer los endpoints de gestión de imágenes existentes cierra el ciclo de gestión del vendedor definido en el roadmap `P0`.

## What Changes

- **Backend (`bidmaster-ws`):**
  - Ampliar `ImageUseCase`, `ImageService` e `ImageController` para gestionar imágenes ya almacenadas de una subasta:
    - `PATCH /api/images/{id}/primary`: marca una imagen existente como portada (`isPrimary = true`) y desmarca las demás de la misma referencia de forma transaccional, validando propiedad del vendedor.
    - `PATCH /api/images/reference/{referenceId}/order`: reordena (`displayOrder = 0..N-1`) las imágenes de una subasta según la lista de IDs enviada, validando propiedad y pertenencia a la referencia.
    - `DELETE /api/images/{id}`: validar propiedad del vendedor o autor cuando exista `Principal` autenticado (o parámetro opcional `userId`), manteniendo compatibilidad con tests anónimos heredados hasta el endurecimiento `P4`.
  - Mejorar `AuctionController.updateAuctionFromDTO` / `AuctionCategory` para resolver categorías tanto por nombre de enum (`ELECTRONICS`) como por nombre visible (`Electrónica`) y rechazar valores inválidos con `400 Bad Request` (`ProblemDetail`) en lugar de ignorarlos silenciosamente.
- **Frontend (`bidmaster-front`):**
  - Añadir ruta protegida `/dashboard/my-auctions` (`MyAuctionsPageComponent` / `MyAuctionsComponent`) y enlace **"Mis subastas"** en el sidebar de `DashboardLayoutComponent` y en el menú de usuario de `AuthButtonComponent`.
  - Implementar listado de subastas del vendedor autenticado (`GET /api/auctions?sellerId=...`) con filtros por estado (`ALL`, `DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED`, `CANCELLED`), búsqueda por título y acciones de ciclo de vida según el estado e invariantes de `Auction` (`Publicar`, `Pausar`, `Reanudar`, `Cancelar`, `Eliminar borrador`, `Editar`, `Ver detalle`).
  - Añadir ruta `/dashboard/edit-auction/:id` reutilizando/extendiendo `CreateAuctionComponent` para cargar una subasta `DRAFT`, editar sus datos (`PUT /api/auctions/{id}`), subir nuevas imágenes (`POST /api/images`), eliminar imágenes existentes (`DELETE /api/images/{id}`), cambiar imagen principal (`PATCH /api/images/{id}/primary`) y reordenar miniaturas (`PATCH /api/images/reference/{referenceId}/order`), con opción de **Guardar cambios** o **Guardar y publicar**.
  - Ampliar `AuctionService` (`pauseAuction`, `resumeAuction`, `cancelAuction`) e `ImageService` (`setPrimaryImage`, `reorderImages`).

### Out of Scope (Fuera de alcance)

- Actualizaciones en tiempo real por SSE, historial de pujas en vivo y datos reales en `MyBidsComponent` / `FavoritesComponent` (corresponden a la siguiente SDD de `P0`).
- Cierre automático programado (`@Scheduled`), anti-sniping o checkout/liquidación financiera (`P1`).
- Endurecimiento global de `SecurityConfig` retirando `.permitAll()` en `/api/**` (`P4`).
- Edición de campos de negocio en subastas ya publicadas (`ACTIVE`/`PAUSED`), dado que las invariantes de `Auction` solo permiten modificar atributos principales en estado `DRAFT`.

## Capabilities

### New Capabilities
- `seller-auction-management`: Panel del vendedor (`/dashboard/my-auctions`) para listar, filtrar por estado y operar el ciclo de vida de sus subastas (`DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED`, `CANCELLED`), incluyendo la edición integral de borradores (`/dashboard/edit-auction/:id`).

### Modified Capabilities
- `auction-image-management`: Gestión de imágenes existentes en subastas creadas mediante endpoints `PATCH` de portada única y reordenación (`displayOrder`), validación de propiedad en `DELETE /api/images/{id}` con `Principal` y soporte correspondiente en `ImageService` del frontend.

## Impact

- **Dominio y Arquitectura (`docs/architecture/domain-model.md`):** Respeta las invariantes de `Auction` (`publish()`, `pause()` con límite `pausesUsed < 2` y `allowPause`, `resume()`, `cancel()` con liberación de depósito, edición/eliminación restringida a `DRAFT`) y los eventos de dominio `AuctionStatusChangedEvent` emitidos `AFTER_COMMIT`.
- **APIs Backend (`bidmaster-ws`):** `ImageController`, `ImageUseCase`, `ImageService`, `AuctionController`, `AuctionCategory`, DTOs de petición para reordenación, tests unitarios (`ImageServiceTest`) y escenarios Karate (`images-security.feature`, `auctions-lifecycle.feature`).
- **Frontend (`bidmaster-front`):** `app.routes.ts` (y prerender SSR), `DashboardLayoutComponent`, `AuthButtonComponent`, `AuctionService`, `ImageService`, nuevo `MyAuctionsComponent`, extensión de `CreateAuctionComponent` para modo edición, y tests unitarios Jasmine/Karma (`*.spec.ts`).
