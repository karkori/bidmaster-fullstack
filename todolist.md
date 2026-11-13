# BidMaster — Roadmap y Todo List de Mejoras

Lista priorizada de funcionalidades, correcciones y mejoras pendientes, ordenada de **mayor impacto y criticidad (`P0`) a menor (`P4`)**, basada en el estado real de `bidmaster-ws` y `bidmaster-front`.

> **Flujo recomendado:** para cada bloque o feature principal, crear especificación en OpenSpec (`/openspec-propose <nombre-kebab-case>`) antes de implementar y verificar con `./mvnw test` + `npm run build` / `npm test`.

---

## P0 — Flujo completo de producto: Navegación a Detalle, Imágenes y Gestión del Vendedor

Brechas críticas detectadas entre la UI actual y el backend ya existente que impiden completar el ciclo real de publicar y gestionar un producto en subasta.

### 1. Gestión completa de productos para subasta e imágenes
- [x] **Crear `ImageService` en Frontend (`bidmaster-front/src/app/core/services/image.service.ts`)**
  - Exponer `uploadImage(file, type, referenceId)`, `getImagesByReference(referenceId, type)`, `deleteImage(id)` y helper de URL (`/api/images/{id}/raw` o prefirmada).
- [x] **Conectar subida real de imágenes en `CreateAuctionComponent`**
  - Selección múltiple, drag-and-drop, validación de formato/tamaño (PNG/JPG/WEBP/GIF ≤ 5 MB), previsualización de miniaturas segura para SSR, reordenación, eliminación antes de enviar y selección de imagen principal (`isPrimary`).
  - Encadenamiento secuencial de subida de imágenes (`POST /api/images` con `type=AUCTION` y `referenceId`) tras crear la subasta, conservando el borrador ante errores parciales.
- [x] **Alinear formulario de creación con el contrato backend (`CreateAuctionRequestDTO`)**
  - Añadidos controles reales y validación Zod v4 para `depositRequired` y `allowPause`, retirado `reservePrice` del DTO/formulario, y conectados los flujos **Guardar borrador (`DRAFT`)** y **Publicar subasta (`ACTIVE`)** llamando a `POST /api/auctions/{id}/publish`.
- [x] **Crear vista de gestión "Mis Subastas" en Dashboard (`/dashboard/my-auctions`)**
  - Listar las subastas del vendedor autenticado (`GET /api/auctions?sellerId=...`) con filtros por estado (`DRAFT`, `ACTIVE`, `PAUSED`, `FINISHED`, `CANCELLED`).
  - Permitir editar subastas en `DRAFT`, añadir/eliminar/reordenar imágenes o cambiar imagen principal, y ejecutar acciones de ciclo de vida (`Publicar`, `Pausar`, `Reanudar`, `Cancelar`, `Eliminar borrador`).
- [x] **Mejorar `ImageController` con `Principal` (JWT), validación de propiedad en subida e `isPrimary`/`displayOrder`**
  - Resuelto `uploadedBy` desde `Principal` (con fallback `userId`), validada propiedad de la subasta (`404`/`403`) en `POST /api/images`, y añadida unicidad de `isPrimary` y `displayOrder` en `ImageUseCase`/`ImageService`/`ImageResponseDTO`.
- [x] **Endpoints para gestionar imágenes existentes en subastas ya creadas**
  - Añadir endpoints dedicados (`PATCH`) para cambiar `isPrimary` y reordenar `displayOrder` sobre imágenes ya subidas y validar propiedad del vendedor en `DELETE /api/images/{id}`.

### 2. Navegación clicable y página de detalle de producto completa
- [x] **Corregir `AuctionCardComponent` (navegación, estados reales e imagen fallback)**
  - Enlace `[routerLink]="['/auctions', auction().slug || auction().id]"`, mapeo de estados reales (`ACTIVE`, `DRAFT`, `PAUSED`, `FINISHED`, `CANCELLED`) y fallback `/no-image.svg`.
- [ ] **Navegación clicable desde `MyBidsComponent` y `FavoritesComponent`**
  - Conectar `viewAuction(auctionId)` y `placeBid(auctionId)` en `MyBidsComponent` (actualmente hacen `console.log`) para navegar a `/auctions/:id`.
  - Conectar `placeBid(auctionId)` y enlaces de tarjetas en `FavoritesComponent` hacia `/auctions/:id`.
- [ ] **Completar `AuctionDetailComponent` (`/auctions/:idOrSlug`)**
  - [x] **Galería de imágenes y estados reales:** mostrar todas las `auction.images` con selector de miniaturas clicables y reconocimiento de `FINISHED`, `PAUSED` y `CANCELLED`.
  - [ ] **Actualización en tiempo real (SSE):** suscribirse a `GET /api/auctions/{id}/stream` para actualizar precio actual, `totalBids`, estado (`PAUSED`/`FINISHED`/`ACTIVE`) y cuenta atrás sin recargar la página.
  - [ ] **Historial de pujas en vivo:** consumir `GET /api/auctions/{id}/bids` y mostrar lista de ofertas recientes (pujador, importe, fecha).
  - [ ] **Acciones rápidas según rol:** botones de pausar/reanudar/cancelar/publicar/editar para el vendedor (`isOwner`), y botón de seguir/favorito para compradores.
  - [ ] **Conectar "Mis Pujas" con datos reales:** reemplazar `getMockBids()` en `MyBidsComponent` por `GET /api/auctions/bids/user/{userId}`.

---

## P1 — Motor automático de subastas, Anti-Sniping y Liquidación Financiera

Funcionalidades esenciales de negocio para que las subastas operen de forma autónoma y completen el ciclo económico.

- [ ] **Cierre automático de subastas vencidas (`@Scheduled`)**
  - Habilitar `@EnableScheduling` en `BidMasterApplication` y crear scheduler periódico que cierre subastas `ACTIVE` con `endDate <= now` mediante `finishAuction`, marcando la puja ganadora (`WON`) y emitiendo `AuctionStatusChangedEvent` por SSE.
  - Añadir índice en `auctions(status, end_date)` vía migración Flyway si aplica y tests de integración/scheduler.
- [ ] **Anti-sniping (auto-extensión en los últimos minutos)**
  - Configurar ventana de extensión en dominio (`Auction.placeBid`): si entra una puja cuando falta menos de un umbral configurable (ej. 2 minutos), extender `endDate` automáticamente y notificar por SSE.
- [ ] **Liquidación / Checkout de subasta ganada**
  - Añadir flujo para que el ganador confirme y pague la subasta finalizada aplicando `depositBlocked` como parte del pago, descontando el saldo restante, acreditando al vendedor y registrando la transacción/orden (`V{N}__create_orders_and_transactions.sql`).
  - Gestionar penalización / plazo límite de pago si el ganador no completa la liquidación.
- [ ] **Historial de movimientos de saldo en Perfil**
  - Registrar recargas, bloqueos por depósito, desbloqueos al ser sobrepujado y liquidaciones finales para mostrarlos en `ProfileComponent`.

---

## P2 — Reemplazar pantallas mock del Dashboard por features reales

El layout y las rutas del dashboard ya existen en Angular, pero cargan arrays de prueba con `setTimeout`.

- [ ] **Sistema de notificaciones en tiempo real (Fullstack)**
  - Backend: entidad `Notification`, migración Flyway, listener de `BidPlacedEvent` (aviso al `previousWinnerId` por puja superada) y `AuctionStatusChangedEvent` (subasta ganada, finalizada, pausada, cancelada), endpoints REST (`GET`, marcar leída, eliminar) y stream SSE por usuario.
  - Frontend: conectar `NotificationsComponent` y contador de no leídas en `HeaderComponent`.
- [ ] **Favoritos / Watchlist real (Fullstack)**
  - Backend: entidad/tabla `favorites (user_id, auction_id, created_at)` y endpoints `GET /api/favorites`, `POST /api/favorites/{auctionId}`, `DELETE /api/favorites/{auctionId}`.
  - Frontend: conectar `FavoritesComponent` (quitando `getMockFavorites()`) y añadir botón de corazón/seguir en `AuctionCardComponent` y `AuctionDetailComponent`.
- [ ] **Mensajería comprador–vendedor (Fullstack)**
  - Backend: hilos y mensajes vinculados a una subasta entre pujadores/comprador y vendedor, con paginación y marcado de lectura.
  - Frontend: conectar `MessagesComponent` con hilos reales y botón "Preguntar al vendedor" desde `AuctionDetailComponent`.

---

## P3 — Búsqueda avanzada, Paginación y Puja Automática (Proxy Bidding)

Mejoras clave de usabilidad y competitividad en subastas.

- [ ] **Búsqueda textual, ordenación y paginación real**
  - Backend: extender `AuctionController.findAuctions` y `AuctionRepositoryAdapter.findByFilters` con búsqueda textual (`q` sobre título y descripción), ordenación (`endingSoon`, `mostBids`, `priceAsc`, `priceDesc`, `newest`) y paginación (`page`, `size` con `Page<AuctionResponseDTO>`).
  - Frontend: conectar `SearchBarComponent`, `FilterBarComponent` y paginador en `HomeComponent`.
- [ ] **Puja automática / Puja máxima (Proxy Bidding)**
  - Permitir que el comprador defina un monto máximo secreto y que el motor puje automáticamente por el incremento mínimo cada vez que otro usuario lo supere, respetando saldo/depósito y límites de concurrencia (`@Version`).
- [ ] **Sistema de valoraciones y reputación post-subasta**
  - Permitir que comprador y vendedor se valoren tras cerrar la transacción, actualizando el campo `User.reputation` ya existente en el modelo.

---

## P4 — Endurecimiento de Seguridad, Administración y Deuda Técnica

Consolidación para producción y limpieza de configuraciones heredadas.

- [ ] **Endurecimiento de `SecurityConfig` y autorización por JWT/Roles**
  - Retirar `.requestMatchers("/api/**").permitAll()` en `SecurityConfig`, exigiendo JWT en endpoints privados/mutaciones y manteniendo públicos solo `login`, `register` y `GET` públicos de subastas/imágenes.
  - Eliminar fallback permisivo de `resolveCurrentUser` en `AuctionController` / `UserController` y restringir `finishAuction`, `balance/add` y operaciones de administración por rol/propietario, actualizando la suite Karate E2E.
- [ ] **Panel de Administración (Rutas comentadas en `app.routes.ts`)**
  - Habilitar `/dashboard/admin/users` y `/dashboard/admin/auctions` con guard de rol `ADMIN` para moderar subastas, revisar reportes de fraude y gestionar estados de usuarios (`SUSPENDED`, `ACTIVE`).
- [ ] **Actualizar Dockerfiles y `Makefile` al stack real**
  - Migrar `bidmaster-ws/Dockerfile` de `eclipse-temurin:17-jdk-alpine` a **JDK 25**.
  - Migrar `bidmaster-front/Dockerfile` de `oven/bun:1` / `bun.lock` a **Node 24 + `npm`**.
  - Actualizar `Makefile` (`frontend-run` usando `npm start`, `frontend-lint` o scripts reales de `package.json`).
