# auction-image-management Specification

## Purpose

Definir el comportamiento verificable para la subida segura de imágenes de subasta, gestión de metadatos de visualización (`isPrimary`, `displayOrder`), resolución del usuario propietario y su consumo desde el cliente Angular.

## Requirements

### Requirement: Subida segura de imágenes de subasta con metadatos de orden y portada

El sistema MUST permitir subir imágenes asociadas a una subasta mediante `POST /api/images` (`multipart/form-data`), validando su firma binaria (magic bytes), resolviendo el usuario propietario desde `Principal` o `userId` y persistiendo los metadatos `isPrimary` y `displayOrder`.

#### Scenario: Subida exitosa especificando imagen principal y orden desde usuario autenticado
- **GIVEN** un usuario autenticado con JWT válido que es propietario de una subasta existente en estado `DRAFT` o `ACTIVE`
- **WHEN** envía `POST /api/images` con `file` válido (PNG/JPEG/WEBP/GIF), `type=AUCTION`, `referenceId=<auctionId>`, `isPrimary=true` y `displayOrder=0` sin enviar `userId`
- **THEN** el backend resuelve `uploadedBy` desde `Principal`, valida la firma binaria con `SecurityScanPort`, almacena la imagen y su miniatura, responde `201 Created` con `ImageResponseDTO` (`primary=true`, `displayOrder=0`, `imageUrl` no nulo) y la imagen queda asociada a la subasta

#### Scenario: Unicidad de imagen principal al subir una nueva portada
- **GIVEN** una subasta que ya tiene una imagen previa marcada con `isPrimary=true`
- **WHEN** el vendedor sube una segunda imagen para la misma subasta con `isPrimary=true` y `displayOrder=1`
- **THEN** el servicio desmarca `isPrimary=false` en la imagen anterior y guarda la nueva imagen como `isPrimary=true`, de modo que `GET /api/auctions/{id}` y `GET /api/images/reference/{id}?type=AUCTION` devuelven exactamente una imagen principal

#### Scenario: Retrocompatibilidad con parámetro explícito userId sin Principal
- **GIVEN** un cliente de API o test E2E sin cabecera `Authorization`
- **WHEN** envía `POST /api/images` con `file` válido, `type=AUCTION` y parámetro `userId=<uuid>`
- **THEN** el backend acepta `userId`, almacena la imagen con `isPrimary=false` y `displayOrder=0` por defecto, y responde `201 Created`

#### Scenario: Rechazo por archivo falsificado o tipo no permitido
- **GIVEN** un archivo cuya extensión o `Content-Type` declara `image/png` pero sus magic bytes corresponden a texto/PHP o ejecutable
- **WHEN** se envía `POST /api/images` con dicho archivo
- **THEN** el backend rechaza la petición con `400 Bad Request` en formato RFC 7807 `ProblemDetail` (`title: "Solicitud no válida"` o `"Error de validación de seguridad"`) y no persiste metadatos ni objetos en almacenamiento

#### Scenario: Rechazo por falta de identificación de usuario o formato inválido en cliente
- **GIVEN** una petición `POST /api/images` sin `Principal` ni parámetro `userId` (o en frontend un archivo superior a 5 MB o fuera de `PNG`/`JPG`/`WEBP`/`GIF`)
- **WHEN** se intenta subir la imagen
- **THEN** el backend responde `400 Bad Request` (`ProblemDetail`) cuando falta identificación de usuario, y el frontend (`ImageService` / `CreateAuctionComponent`) bloquea la carga en cliente mostrando un mensaje de error claro antes de invocar la API

#### Scenario: Rechazo cuando un usuario autenticado intenta adjuntar imagen a una subasta ajena
- **GIVEN** un usuario autenticado `B` distinto del `sellerId` de la subasta `A`
- **WHEN** `B` envía `POST /api/images` con `type=AUCTION` y `referenceId=<idSubastaA>`
- **THEN** el backend responde `403 Forbidden` y no almacena ni asocia la imagen a la subasta

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
