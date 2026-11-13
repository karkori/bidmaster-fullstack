# Spec Delta

## MODIFIED Requirements

### Requirement: Servicio frontend `ImageService` y fallback de URL de visualización

El frontend SHALL disponer de un servicio dedicado `ImageService` para gestionar la subida, consulta, borrado, cambio de portada, reordenación y resolución segura de URLs de imágenes compatible con SSR.

#### Scenario: Subida y resolución de URL desde `ImageService`
- **GIVEN** un `File` válido seleccionado por el usuario y un `referenceId` de subasta recién creada
- **WHEN** `ImageService.uploadImage(file, 'AUCTION', referenceId, { isPrimary, displayOrder })` envía `FormData` a `POST ${environment.apiUrl}/images`
- **THEN** devuelve un `Observable<ImageResponse>` tipado con `id`, `imageUrl`, `thumbnailUrl`, `primary` y `displayOrder`, y su helper `resolveImageUrl` devuelve la URL absoluta válida o `/api/images/{id}/raw` si `imageUrl` viene vacía o relativa

#### Scenario: Cambio de portada y reordenación desde `ImageService`
- **GIVEN** una subasta existente con múltiples imágenes persistidas
- **WHEN** el cliente llama a `ImageService.setPrimaryImage(imageId)` o `ImageService.reorderImages(referenceId, imageIds, 'AUCTION')`
- **THEN** envía `PATCH ${environment.apiUrl}/images/{id}/primary` o `PATCH ${environment.apiUrl}/images/reference/{referenceId}/order?type=AUCTION` con `{ imageIds }` y devuelve los DTOs actualizados con `primary` y `displayOrder` consistentes

## ADDED Requirements

### Requirement: Gestión de portada, orden y borrado seguro sobre imágenes existentes

El sistema MUST exponer endpoints para marcar una imagen existente como portada (`PATCH /api/images/{id}/primary`), reordenar imágenes de una referencia (`PATCH /api/images/reference/{referenceId}/order`) y validar propiedad del vendedor en `DELETE /api/images/{id}`.

#### Scenario: Cambio atómico de imagen principal mediante PATCH
- **GIVEN** una subasta propia con dos imágenes persistidas (`img1` con `primary=true`, `img2` con `primary=false`)
- **WHEN** el vendedor autenticado envía `PATCH /api/images/{img2}/primary`
- **THEN** el backend verifica que pertenece a su subasta, desmarca `img1` (`primary=false`), marca `img2` (`primary=true`) en una misma transacción y responde `200 OK` con `ImageResponseDTO` (`primary=true`)

#### Scenario: Reordenación de imágenes mediante PATCH con validación de pertenencia
- **GIVEN** una subasta propia con imágenes `[img1, img2, img3]`
- **WHEN** el vendedor autenticado envía `PATCH /api/images/reference/{auctionId}/order?type=AUCTION` con `{ "imageIds": [img3, img1, img2] }`
- **THEN** el backend valida propiedad y pertenencia completa de los IDs a la referencia, actualiza `displayOrder` (`0, 1, 2`) y responde `200 OK` con `List<ImageResponseDTO>` ordenada; si faltan IDs o hay alguno ajeno a la subasta, responde `400 Bad Request` (`ProblemDetail`)

#### Scenario: Validación de propiedad en eliminación y modificación de imágenes
- **GIVEN** una imagen asociada a la subasta del vendedor `A`
- **WHEN** un usuario autenticado `B` distinto de `A` envía `DELETE /api/images/{id}`, `PATCH /api/images/{id}/primary` o `PATCH /api/images/reference/{auctionId}/order`
- **THEN** el backend rechaza la operación con `403 Forbidden` sin alterar ni eliminar registros; cuando `DELETE /api/images/{id}` se invoque sin `Principal` ni `userId` (escenario anónimo heredado), mantiene compatibilidad hasta el endurecimiento `P4`
