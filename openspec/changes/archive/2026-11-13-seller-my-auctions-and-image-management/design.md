# Design

## Context

Ver `proposal.md` para la motivación y el alcance funcional. Actualmente:
- En `bidmaster-ws`, `AuctionController` ya expone `GET /api/auctions?sellerId=...`, `PUT /api/auctions/{id}`, `POST /api/auctions/{id}/publish`, `POST /api/auctions/{id}/pause`, `POST /api/auctions/{id}/resume`, `POST /api/auctions/{id}/cancel` y `DELETE /api/auctions/{id}` con comprobación de propietario (`auction.getSellerId().equals(user.getId())`). Sin embargo:
  - `AuctionController.updateAuctionFromDTO` captura `IllegalArgumentException` al parsear `category` y `AuctionResponseDTO` devuelve `category` usando `displayName` (`"Electrónica"`) en lugar del nombre de enum (`"ELECTRONICS"`).
  - `ImageController` expone `POST /api/images`, `GET /api/images/reference/{referenceId}` y `DELETE /api/images/{id}`, pero carece de operaciones `PATCH` para cambiar `isPrimary` o reordenar `displayOrder` sobre imágenes ya persistidas y no verifica propiedad en `DELETE /api/images/{id}`.
- El esquema de base de datos (`V4__create_images_table.sql`, `ImageEntity`) ya incluye `is_primary`, `display_order` y `@Version`, por lo que **no se requieren nuevas migraciones Flyway** (`ddl-auto: validate` permanece intacto).
- En `bidmaster-front`, `CreateAuctionComponent` ya gestiona validación Zod v4, cola de imágenes y publicación, y `ImageService` / `AuctionService` consumen los endpoints base, pero faltan los métodos de ciclo de vida/imágenes restantes, el modo edición y la vista `/dashboard/my-auctions`.

## Goals / Non-Goals

**Goals:**
- Completar en `bidmaster-ws` los puertos (`ImageUseCase`), servicio transaccional (`ImageService`), DTOs (`ReorderImagesRequestDTO`) y endpoints (`ImageController`) respetando estrictamente la arquitectura hexagonal y el optimistic locking (`@Version`).
- Corregir la resolución de `AuctionCategory` tanto por nombre de enum como por `displayName` para evitar desajustes al cargar borradores en edición y rechazar valores desconocidos con `400 Bad Request` (`ProblemDetail`).
- Implementar `/dashboard/my-auctions` y `/dashboard/edit-auction/:id` en Angular 21 (standalone, Signals, `input()`/`output()`, `@if`/`@for`, SSR-safe, Tailwind CSS v4) reutilizando componentes compartidos (`PageHeaderComponent`, `FilterBarComponent`, `StatusBadgeComponent`, `EmptyStateComponent`, `LoadingSpinnerComponent`).

**Non-Goals:**
- Modificar `SecurityConfig` para retirar `.permitAll()` en `/api/**` (pertenece a `P4`); se mantiene la resolución mediante `Principal` con compatibilidad anónima controlada donde ya la usan los tests heredados.
- Implementar streaming SSE en tiempo real sobre la lista del vendedor o detalle (`AuctionDetailComponent` SSE corresponde a la siguiente SDD).
- Soportar subida de imágenes de perfil/categoría en UI (solo `type=AUCTION` en este alcance).

## Decisions

### 1. Contratos REST de gestión de imágenes existentes (`ImageController` / `ImageService`)
- **Decisión:** Exponer dos endpoints `PATCH` semánticos y un `DELETE` endurecido:
  1. `PATCH /api/images/{id}/primary` (parámetro opcional `userId` solo como fallback cuando `Principal == null`):
     - Resuelve la imagen (`404` si no existe). Si `type == AUCTION` y `referenceId != null`, valida propiedad del vendedor (`403 Forbidden` si el solicitante no es `sellerId`).
     - En `ImageService.setPrimaryImage(UUID imageId)` (`@Transactional`), si `referenceId != null`, desmarca `isPrimary` en las imágenes hermanas (`findByReferenceIdAndType`) y marca `setAsPrimary()` en la imagen objetivo preservando su `version` JPA para optimistic locking. Devuelve `200 OK` con `ImageResponseDTO`.
  2. `PATCH /api/images/reference/{referenceId}/order?type=AUCTION`:
     - Recibe `ReorderImagesRequestDTO` (`@NotEmpty List<UUID> imageIds`, opcional `UUID userId` cuando `Principal == null`).
     - Valida existencia de la subasta y propiedad (`403 Forbidden`) cuando `type == AUCTION`.
     - En `ImageService.reorderImages(UUID referenceId, ImageType type, List<UUID> orderedIds)` (`@Transactional`), verifica que todos los IDs existen, pertenecen a `(referenceId, type)`, no hay duplicados y cubren las imágenes de la referencia (`400 Bad Request` en caso contrario), asignando `displayOrder = 0..N-1` y guardando en orden. Devuelve `200 OK` con `List<ImageResponseDTO>`.
  3. `DELETE /api/images/{id}` (parámetro opcional `userId`):
     - Si `Principal != null` o se envía `userId`, resuelve el usuario solicitante y verifica propiedad (sea `sellerId` de la subasta si `type == AUCTION`, o `uploadedBy` en otro caso), devolviendo `403 Forbidden` si no coincide.
     - Si no hay `Principal` ni `userId`, mantiene compatibilidad temporal con el escenario anónimo heredado de Karate (`images-security.feature`) hasta `P4`.
- **Alternativas consideradas:**
  - *Reenviar multipart completo o borrar y resubir imágenes al reordenar*: descartado por ineficiencia, pérdida de URLs/objetos S3 y riesgo de fallos parciales.
  - *Exigir siempre JWT en `DELETE /api/images/{id}`*: rompería el escenario heredado de `images-security.feature` antes del endurecimiento `P4`; la validación condicionada a `Principal`/`userId` protege todo el flujo real del frontend manteniendo la suite verde.

### 2. Normalización de `AuctionCategory` en `PUT /api/auctions/{id}` y frontend
- **Decisión:** Ampliar `AuctionCategory.fromString(String)` (o método helper equivalente) para aceptar tanto el nombre de enum (`ELECTRONICS`) como el `displayName` insensible a mayúsculas/acentos (`"Electrónica"`), y en `AuctionController.updateAuctionFromDTO` lanzar `IllegalArgumentException` (que `GlobalExceptionHandler` convierte en `400 Bad Request` RFC 7807 `ProblemDetail`) si la categoría no corresponde a ninguna constante. En frontend, añadir un mapper consistente (`ELECTRONICS` ↔ `"Electrónica"`) al prellenar el formulario de edición desde `Auction`.
- **Alternativas consideradas:** Cambiar `AuctionResponseDTO.category` de `displayName` a nombre de enum; descartado porque `auctions-lifecycle.feature` (línea 184) y vistas existentes ya esperan `"Coleccionables"`.

### 3. Reutilización de `CreateAuctionComponent` para edición de `DRAFT` vs componente separado
- **Decisión:** Reutilizar `CreateAuctionComponent` detectando `:id` en `ActivatedRoute` (`/dashboard/edit-auction/:id`) en lugar de duplicar el formulario reactivo y la lógica de validación Zod/subida de imágenes. En modo edición:
  - Carga la subasta (`GET /api/auctions/{id}`), verifica en cliente que `status === 'DRAFT'` y `sellerId === currentUser.id` (mostrando aviso claro y enlace de retorno si no aplica).
  - Separa visualmente las **imágenes ya persistidas** (`existingImages = signal<AuctionImage[]>`) —que disparan `PATCH .../primary`, `PATCH .../order` y `DELETE /api/images/{id}` (diferido al guardar o inmediato con confirmación clara; se opta por aplicar cambios de imágenes existentes de forma explícita y atómica al guardar o con acciones directas y feedback claro)— de las **nuevas imágenes pendientes** (`pendingImages`, subidas por `POST /api/images` tras `PUT /api/auctions/{id}`).
  - **Nota de diseño UX:** Aplicar cambios de portada/orden/borrado de imágenes existentes en sincronía al guardar (o con botones directos cuyo estado refleja la API) evita estados huérfanos; en modo edición, permitir que **Guardar cambios** ejecute `PUT /api/auctions/{id}` -> borrados/altas/orden/portada -> y opcionalmente `POST /api/auctions/{id}/publish` si el usuario elige **Guardar y publicar**, mantiene una transacción lógica predecible igual que en creación.
- **Alternativas consideradas:** Crear un segundo componente `EditAuctionComponent` copiando la plantilla de 400 líneas; descartado por duplicación y riesgo de divergencia en reglas Zod.

### 4. Vista `/dashboard/my-auctions` y confirmaciones sin diálogos nativos
- **Decisión:** Crear `MyAuctionsComponent` (`bidmaster-front/src/app/components/my-auctions/`) y su página wrapper `MyAuctionsPageComponent` (`bidmaster-front/src/app/pages/dashboard/my-auctions-page/`), usando Signals para estado (`auctions`, `statusFilter`, `searchTerm`, `actionLoadingId`, `pendingConfirmation`, `feedbackMessage`, `errorMessage`).
- Las acciones destructivas o irreversibles (`Eliminar borrador`, `Cancelar subasta`) usarán confirmación inline en la tarjeta/fila (en lugar de `window.confirm`), garantizando compatibilidad SSR, accesibilidad y testabilidad unitaria en Karma/Jasmine.
- Recordar `authInterceptor`: ante un `401`/`403` del backend el interceptor cierra sesión y redirige a `/auth/login`, por lo que `MyAuctionsComponent` solo mostrará los botones permitidos según estado e invariantes (`DRAFT`: Editar/Publicar/Eliminar; `ACTIVE`: Pausar si `allowPause && pausesUsed < 2`, Cancelar, Ver; `PAUSED`: Reanudar, Cancelar, Ver; `FINISHED`/`CANCELLED`: Ver).

## Risks / Trade-offs

- **[Conflicto de concurrencia (`@Version`) al reordenar o cambiar portada en paralelo]** → Todas las mutaciones en `ImageService` usan `@Transactional` y preservan `Image.version` al persistir mediante `ImagePersistenceAdapter`; `GlobalExceptionHandler` responde `409 Conflict` (`ProblemDetail`) si hay colisión concurrente.
- **[Diferencia de zonas horarias en `<input type="datetime-local">` al editar `endDate` de un `DRAFT`]** → Formatear la fecha recibida del backend a `YYYY-MM-DDTHH:mm` local del navegador antes de hacer `patchValue`, igual que el valor por defecto del constructor de `CreateAuctionComponent`.
- **[SSR / Prerendering con nueva ruta parametrizada `/dashboard/edit-auction/:id`]** → Comprobar con `npm run build` que el builder SSR/prerender procesa correctamente la nueva ruta protegida por `authGuard` y todas las lecturas de `currentUser` / carga HTTP se ejecutan protegidas con `isPlatformBrowser`.
