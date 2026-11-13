# Tasks

## 1. Backend — Aplicación y Puertos (`ImageUseCase`, `ImageService`)

- [x] 1.1 Extender `ImageUseCase` e `ImageService` con sobrecarga `storeImage(..., boolean isPrimary, int displayOrder)` manteniendo compatibilidad con la firma de 6 parámetros, asegurando unicidad de `isPrimary` (desmarcar imágenes previas de la misma referencia cuando `isPrimary=true`) y asignando `displayOrder`; verificar compilación del módulo con `cd bidmaster-ws && ../scripts/setup-env.sh || true` (usando `source scripts/setup-env.sh`) y `./mvnw test-compile`.
- [x] 1.2 Añadir pruebas unitarias JUnit 5 para `ImageService` cubriendo asignación de `isPrimary`, desmarcado automático de portada previa, `displayOrder` y rechazo de contenido inválido; verificar ejecutando `cd bidmaster-ws && ./mvnw test -Dtest=ImageServiceTest`.

## 2. Backend — Adaptadores Web (`ImageController`, `ImageResponseDTO`, `AuctionController`) y E2E Karate

- [x] 2.1 Actualizar `ImageResponseDTO` (exponer `primary` y `displayOrder` más fallback `/api/images/{id}/raw` si `imageUrl` es nulo o vacío) y `AuctionController.enrichAndConvertToDTO` (fallback `/api/images/{id}/raw` cuando `image.getUrl()` esté vacío); verificar compilación con `./mvnw test-compile`.
- [x] 2.2 Actualizar `ImageController.uploadImage` para aceptar parámetros opcionales `userId`, `isPrimary` (default `false`) y `displayOrder` (default `0`), resolver `uploadedBy` desde `Principal` cuando `userId` se omite (devolviendo `400 Bad Request` `ProblemDetail` si faltan ambos) y validar propiedad de la subasta (`404` si no existe, `403 Forbidden` si `sellerId != currentUser.id`); verificar que `AuctionController` y `ImageController` respetan las reglas RFC 7807.
- [x] 2.3 Ampliar `bidmaster-ws/src/test/resources/karate/images-security.feature` con escenarios de subida autenticada con JWT sin `userId` (`isPrimary=true`, `displayOrder`), reemplazo de portada única, rechazo `403` por subasta ajena y `400` sin usuario; verificar suite completa ejecutando `source scripts/setup-env.sh && cd bidmaster-ws && ./mvnw test` (incluyendo dominio, concurrencia y Karate E2E).

## 3. Frontend — Modelos, Validación Zod v4 y `ImageService`

- [x] 3.1 Alinear `CreateAuctionDto` en `bidmaster-front/src/app/shared/models/auction.model.ts` (añadir `depositRequired?: number`, retirar `reservePrice`, añadir interfaces `ImageResponse` / `ImageUploadOptions`) y crear `bidmaster-front/src/app/shared/validators/auction.schemas.ts` con el esquema Zod v4 alineado con las invariantes de `Auction` (`title` 5–100, `description` 20–1000, `initialPrice > 0`, `depositRequired >= 0`, `endDate >= ahora + 1h`); verificar tipado con `cd bidmaster-front && npx tsc --noEmit`.
- [x] 3.2 Crear `bidmaster-front/src/app/core/services/image.service.ts` (`uploadImage`, `getImagesByReference`, `deleteImage`, `resolveUrl` compatible con SSR) y sus pruebas unitarias en `image.service.spec.ts` verificando `FormData`, query/form params y resolución de URLs; validar ejecutando `cd bidmaster-front && CHROME_BIN=/usr/bin/chromium npm test`.

## 4. Frontend — Flujo de Creación, Subida de Imágenes y Publicación (`CreateAuctionComponent`)

- [x] 4.1 Conectar el selector de archivos y zona drag-and-drop en `CreateAuctionComponent` (`.ts`/`.html`) con validación en cliente (PNG/JPG/WEBP/GIF ≤ 5 MB), previsualización de miniaturas segura para SSR, eliminación antes de enviar y selección de imagen principal (`isPrimary`) y orden (`displayOrder`).
- [x] 4.2 Alinear el formulario de `CreateAuctionComponent` con `CreateAuctionRequestDTO` y Zod v4 (`depositRequired`, `allowPause`, retirada de `reservePrice`) y conectar los botones **"Guardar borrador"** (`DRAFT`) y **"Publicar subasta"** (`POST /api/auctions` -> `POST /api/images` secuencial -> `POST /api/auctions/{id}/publish`), conservando `createdAuctionId` si falla una imagen y navegando a `/auctions/:slugOrId` al finalizar; verificar compilación SSR/browser con `cd bidmaster-front && npm run build`.
- [x] 4.3 Añadir pruebas unitarias para `CreateAuctionComponent` (validaciones Zod/Reactive Forms, selección/eliminación de imágenes y flujo guardar borrador vs crear y publicar) y ejecutar `cd bidmaster-front && CHROME_BIN=/usr/bin/chromium npm test`.

## 5. Frontend — Tarjetas (`AuctionCardComponent`) y Galería / Estados en Detalle (`AuctionDetailComponent`)

- [x] 5.1 Añadir placeholder `bidmaster-front/public/no-image.svg`, corregir `AuctionCardComponent` (`.ts`/`.html`) para mapear todos los estados reales (`ACTIVE`, `DRAFT`, `PAUSED`, `FINISHED`, `CANCELLED`) y resolver imagen con fallback (`primary` -> `images[0].url` -> `/no-image.svg`); añadir spec unitario para verificar estado e imagen.
- [x] 5.2 Actualizar `AuctionDetailComponent` (`.ts`/`.html`) para reconocer `FINISHED`, `PAUSED` y `CANCELLED` y añadir galería básica con miniaturas clicables cuando `auction.images.length > 1`; verificar con `cd bidmaster-front && npm run build && CHROME_BIN=/usr/bin/chromium npm test`.

## 6. Verificación Integral e Integración Manual Frontend ↔ Backend

- [x] 6.1 Ejecutar verificación automatizada completa en ambos paquetes: `source scripts/setup-env.sh && cd bidmaster-ws && ./mvnw test` y `cd bidmaster-front && npm run build && CHROME_BIN=/usr/bin/chromium npm test`.
- [x] 6.2 Realizar prueba manual de integración Frontend ↔ Backend levantando infraestructura/servicios (`docker compose up -d` o perfil local según disponibilidad del entorno, backend `:8080` y frontend `:4200`), verificando en sesión real: login de vendedor, creación de borrador con imágenes, creación y publicación inmediata con múltiples imágenes y selección de portada, visualización en `HomeComponent` (`AuctionCardComponent`), clic hacia `/auctions/:slugOrId` y cambio de miniaturas en `AuctionDetailComponent`; actualizar estado de tareas completadas en `todolist.md`.

## Workflow follow-up

- Archivar el cambio en OpenSpec (`/openspec-archive-change`) una vez completadas y validadas todas las tareas para consolidar `specs/auction-image-management` y `specs/auction-creation-and-listing`.
- Abrir el siguiente SDD incremental desde `todolist.md` (por ejemplo `seller-my-auctions-dashboard` o `auction-detail-live-sse-and-bids`).
