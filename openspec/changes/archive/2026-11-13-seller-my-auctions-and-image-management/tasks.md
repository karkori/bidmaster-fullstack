# Tasks

## 1. Dominio y Aplicación Backend (`bidmaster-ws`)

- [x] 1.1 Ampliar `AuctionCategory.fromString` en `domain/model/auction/AuctionCategory.java` para resolver categorías tanto por nombre de enum (`ELECTRONICS`) como por `displayName` (`"Electrónica"`) y verificar su comportamiento con pruebas unitarias en `AuctionDomainTest`.
- [x] 1.2 Añadir `setPrimaryImage(UUID imageId)` y `reorderImages(UUID referenceId, ImageType type, List<UUID> orderedIds)` en `ImageUseCase` e `ImageService` (`@Transactional`, validando existencia, pertenencia completa sin duplicados, unicidad de `isPrimary` y preservación de `version` para optimistic locking) y cubrir casos de éxito y error en `ImageServiceTest`.

## 2. Adaptadores Web Backend y Suite Karate E2E (`bidmaster-ws`)

- [x] 2.1 Crear `ReorderImagesRequestDTO` (`@NotEmpty List<UUID> imageIds`) e implementar en `ImageController` los endpoints `PATCH /api/images/{id}/primary`, `PATCH /api/images/reference/{referenceId}/order` y validación de propiedad en `DELETE /api/images/{id}` (con `Principal` o `userId`, manteniendo retrocompatibilidad cuando ambos son `null`), verificando la compilación del módulo.
- [x] 2.2 Actualizar `AuctionController.updateAuctionFromDTO` para normalizar categorías con `AuctionCategory.fromString` lanzando `IllegalArgumentException` (`400 ProblemDetail`) ante valores desconocidos, y exponer `pausesUsed` en `AuctionResponseDTO` (`enrichAndConvertToDTO`), verificando la respuesta JSON del endpoint.
- [x] 2.3 Ampliar `images-security.feature` (cambio de portada por `PATCH`, reordenación por `PATCH`, rechazo `400` con IDs inválidos/incompletos y `403 Forbidden` de usuario ajeno en `PATCH`/`DELETE`) y `auctions-lifecycle.feature` (edición `PUT` de borrador con `displayName`, rechazo `400` en categoría inválida o subasta `ACTIVE`, `cancel` y `DELETE` borrador), y ejecutar `cd bidmaster-ws && ./mvnw test` verificando 0 fallos.

## 3. Servicios, Modelos y Rutas Frontend (`bidmaster-front`)

- [x] 3.1 Añadir `pausesUsed?: number` y `ReorderImagesRequest` en `shared/models/auction.model.ts`, `pauseAuction(id)`, `resumeAuction(id)` y `cancelAuction(id)` en `AuctionService`, y `setPrimaryImage(imageId)` y `reorderImages(referenceId, imageIds, type)` en `ImageService`, verificándolos en `auction.service.spec.ts` e `image.service.spec.ts`.
- [x] 3.2 Registrar las rutas protegidas `/dashboard/my-auctions` y `/dashboard/edit-auction/:id` en `app.routes.ts` y añadir el acceso **"Mis subastas"** en `DashboardLayoutComponent` y `AuthButtonComponent`, verificando la compilación TypeScript.

## 4. Interfaz de Gestión del Vendedor y Edición de Borradores (`bidmaster-front`)

- [x] 4.1 Implementar `MyAuctionsComponent` (`components/my-auctions/`) y `MyAuctionsPageComponent` (`pages/dashboard/my-auctions-page/`) con carga SSR-safe por `sellerId`, filtros por estado (`ALL`, `DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED`, `CANCELLED`), búsqueda por título, acciones de ciclo de vida (`Editar`, `Publicar`, `Pausar`, `Reanudar`, `Cancelar`, `Eliminar borrador` con confirmación inline sin `window.confirm`) y manejo de errores `ProblemDetail`; cubrir con `my-auctions.component.spec.ts`.
- [x] 4.2 Extender `CreateAuctionComponent` para modo edición (`/dashboard/edit-auction/:id`): cargar borrador `DRAFT` (mapeando `category` `displayName` -> enum y formateando `endDate`), editar atributos (`PUT /api/auctions/{id}`), gestionar imágenes persistidas (añadir, borrar `DELETE`, fijar portada `PATCH`, reordenar `PATCH`) y soportar **Guardar cambios** o **Guardar y publicar**; cubrir en `create-auction.component.spec.ts`.

## 5. Verificación Fullstack, Integración Real y Seguimiento

- [x] 5.1 Ejecutar la batería automatizada completa (`cd bidmaster-ws && ./mvnw test`, `cd bidmaster-front && npm run build` validando SSR/prerender, y `CHROME_BIN=/usr/bin/chromium npm test -- --watch=false`) confirmando éxito total.
- [x] 5.2 Ejecutar verificación manual E2E en navegador real (`Chromium` contra `bidmaster-ws` + `bidmaster-front`): crear borrador con 2 imágenes -> listar en `/dashboard/my-auctions` con filtro `DRAFT` -> editar datos e imágenes en `/dashboard/edit-auction/:id` (portada, orden, borrado y nueva imagen) -> publicar -> pausar y reanudar desde `/dashboard/my-auctions` verificando 0 errores en consola y red.
- [x] 5.3 Actualizar `todolist.md` marcando como completados `[x]` los ítems `P0` de "Mis Subastas" y endpoints de gestión de imágenes existentes tras superar todas las comprobaciones.
