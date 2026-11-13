# Design

## Context

Motivación y alcance completo en [proposal.md](./proposal.md). El sistema ya cuenta en `bidmaster-ws` con persistencia de imágenes unificada (`V4__create_images_table.sql`, incluyendo `is_primary` y `display_order`), validación por firma binaria (`FileSignatureSecurityAdapter`), almacenamiento S3 en MinIO (`MinioStorageAdapter`) y endpoints REST en `ImageController` y `AuctionController`. En `bidmaster-front`, `CreateAuctionComponent` tiene el HTML de subida sin lógica, `CreateAuctionDto` está desalineado con `CreateAuctionRequestDTO`, no existe `ImageService`, y tanto `AuctionCardComponent` como `AuctionDetailComponent` esperan el estado inexistente `COMPLETED` y una ruta rota `assets/images/no-image.png`.

Restricciones clave:
- Mantener la arquitectura hexagonal de `bidmaster-ws` sin anotaciones Spring/JPA en `domain/`.
- Mantener compatibilidad con `hibernate.ddl-auto=validate` sin añadir migraciones innecesarias.
- Garantizar compatibilidad SSR/prerender en `bidmaster-front` (proteger cualquier uso de APIs de navegador como `URL.createObjectURL` / `FileReader` dentro de handlers de usuario o `isPlatformBrowser`).
- No romper la suite Karate existente (`images-security.feature`, `auctions-lifecycle.feature`).

## Goals / Non-Goals

**Goals:**
- Completar el flujo real vendedor: crear subasta (`DRAFT`) -> subir 0..N imágenes con orden y portada -> publicar opcionalmente (`ACTIVE`) -> redirigir al detalle `/auctions/:slugOrId`.
- Añadir soporte en backend para `isPrimary`, `displayOrder`, resolución de propietario desde `Principal` (con fallback a `userId`) y validación de propiedad sobre la subasta.
- Corregir el mapeo de estados (`FINISHED`, `PAUSED`, `CANCELLED`) e imágenes (portada -> primera imagen -> placeholder `/no-image.svg`) en tarjetas y detalle.

**Non-Goals:**
- Gestión posterior de subastas ("Mis Subastas" `/dashboard/my-auctions`), edición de borradores ni reordenación post-creación.
- Suscripción SSE o historial de pujas en `AuctionDetailComponent`.
- Persistencia de `reservePrice` en dominio/BD (se retira del formulario actual hasta definir su regla de negocio).
- Endurecimiento global de `SecurityConfig`.

## Decisions

1. **Orquestación en dos pasos (crear subasta -> subir imágenes -> publicar opcionalmente) frente a endpoint multipart monolítico:**
   - *Decisión:* Mantener `POST /api/auctions` (JSON) seguido de `POST /api/images` (`multipart/form-data` con `referenceId` y `type=AUCTION`) y opcionalmente `POST /api/auctions/{id}/publish`.
   - *Racional:* Reutiliza los casos de uso existentes (`AuctionUseCase` e `ImageUseCase`), mantiene validación independiente por imagen con `FileSignatureSecurityAdapter` y evita acoplar `AuctionController` a `MultipartFile`.
   - *Alternativa descartada:* Crear un nuevo `POST /api/auctions/with-images` multipart que duplicaría lógica de DTO/validación y complicaría el manejo de errores por archivo.

2. **Resolución de usuario y autorización en `ImageController.uploadImage`:**
   - *Decisión:* Hacer `userId` opcional (`@RequestParam(value = "userId", required = false) UUID userId`) y recibir `Principal`. Si `principal != null`, resolver el usuario autenticado vía `UserUseCase.findUserByUsername(principal.getName())`. Si `principal == null`, usar `userId` (si tampoco viene, responder `400 Bad Request`). Cuando `imageType == ImageType.AUCTION` y `referenceId != null`, verificar que la subasta existe (`404` si no) y, si hay usuario resuelto, validar que `auction.getSellerId().equals(resolvedUserId)` devolviendo `403 Forbidden` en caso contrario.
   - *Racional:* Preserva compatibilidad total con `images-security.feature` (que aún envía `userId` sin JWT porque `SecurityConfig` permite `/api/**`), mientras permite al frontend autenticado omitir `userId` o confiar en el JWT sin exponer suplantación.

3. **Unicidad de `isPrimary` y respuesta en `ImageUseCase` / `ImageService` / `ImageResponseDTO`:**
   - *Decisión:* Añadir sobrecarga en `ImageUseCase.storeImage(..., boolean isPrimary, int displayOrder)` manteniendo la firma de 6 parámetros delegando a `(..., false, 0)`. En `ImageService.storeImage`, si `isPrimary && referenceId != null`, consultar `imageRepositoryPort.findByReferenceIdAndType(referenceId, type)` y desmarcar `unsetPrimary()` + `save()` en las previas que tuvieran `isPrimary == true`. Añadir `primary` (`isPrimary`) y `displayOrder` a `ImageResponseDTO` y asegurar que, si `imageStoragePort.getPublicUrl(...)` devuelve `null` o vacío, la URL de respaldo en DTOs sea `/api/images/{id}/raw`.
   - *Racional:* El esquema SQL `V4` ya posee `is_primary`, `display_order` y el índice `idx_images_primary`, por lo que no hace falta migración Flyway.

4. **Contrato del formulario y modelos Frontend (`CreateAuctionDto`, Zod v4, `ImageService`):**
   - *Decisión:*
     - Alinear `CreateAuctionDto` en `auction.model.ts` agregando `depositRequired?: number` y eliminando `reservePrice` del envío.
     - Crear `bidmaster-front/src/app/shared/validators/auction.schemas.ts` con Zod v4 (`title` 5..100, `description` 20..1000, `category`, `initialPrice > 0`, `minBidIncrement > 0`, `depositRequired >= 0`, `endDate >= now + 1h`, `condition`, `shippingOptions`, `allowPause`, `terms === true`) para validar al enviar y reflejar mensajes coherentes en `CreateAuctionComponent`.
     - Crear `ImageService` (`@core/services/image.service.ts`) con `uploadImage(...)`, `getImagesByReference(...)`, `deleteImage(...)` y `resolveUrl(...)` usando `environment.apiUrl`.
     - En `CreateAuctionComponent`, usar `switchMap` / `concatMap` de RxJS (`from(images).pipe(concatMap(...))`) tras `createAuction()` para subir las imágenes secuencialmente respetando `displayOrder` (`0..N-1`) e `isPrimary`, y luego llamar a `publishAuction(created.id)` solo si el usuario eligió publicar; en cualquier caso redirigir a `['/auctions', finalAuction.slug || finalAuction.id]`.
     - Si falla una subida tras haberse creado el `DRAFT`, mostrar el error sin resetear el formulario ni perder la referencia `createdAuctionId` (si reintenta, o bien informa claramente al usuario que el borrador ya fue guardado con enlace directo para no duplicar subastas).

5. **Placeholder de imagen y estado `FINISHED` en `AuctionCardComponent` y `AuctionDetailComponent`:**
   - *Decisión:* Añadir `bidmaster-front/public/no-image.svg` (servido directamente en `/no-image.svg` según `angular.json` `assets: [{ input: "public" }]`). Actualizar `AuctionCardComponent.imageUrl` para buscar `img.primary`, luego `images[0]?.url`, y por último `'/no-image.svg'`. Actualizar badges para cubrir `ACTIVE`, `DRAFT`, `PAUSED`, `FINISHED` y `CANCELLED`. En `AuctionDetailComponent`, añadir señal/propiedad `selectedImageIndex` para alternar la imagen principal desde las miniaturas y actualizar `auction.status === 'COMPLETED'` a `auction.status === 'FINISHED'`, añadiendo bloques para `PAUSED` y `CANCELLED`.

## Risks / Trade-offs

- **[Fallo al publicar tras subir imágenes parcialmente]** → Si la creación (`DRAFT`) tuvo éxito y falla alguna imagen o `publishAuction`, se conserva el `createdAuctionId` en el componente para ofrecer acceso directo al borrador creado o reintentar sin crear subastas duplicadas.
- **[MinIO no levantado en pruebas manuales con perfil dev sin Docker]** → Documentar en el plan de pruebas manuales la verificación con Docker (`postgres` + `minio`) y asegurar manejo limpio de error HTTP en frontend si storage falla.
- **[Dependencia `http://localhost:9000` en CORS/URLs si MinIO no expone bucket]** → `MinioStorageAdapter.getPublicUrl` configura política pública `s3:GetObject`, y en caso de ausencia de URL pública directa se usa `/api/images/{id}/raw`.
