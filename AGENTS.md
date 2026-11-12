# AGENTS.md — BidMaster Fullstack

Guía operativa para agentes de IA que trabajan en **BidMaster**, una plataforma de subastas online construida como monorepo fullstack.

> **Fuente de verdad:** Ante discrepancias entre documentación antigua (`README.md`, `docs/dev-strategy/*_best_practices.md`) y el código fuente, **prevalece siempre el código real** y las secciones de advertencia de este documento.

---

## 1. Estructura del Repositorio

```text
bidmaster-fullstack/
├── bidmaster-ws/        # Backend: Spring Boot 3.5.16 (Java 25, MVC + Virtual Threads + JPA)
├── bidmaster-front/     # Frontend: Angular 21.2 LTS (SSR, Signals, Tailwind CSS v4, Zod 4)
├── docs/                # Modelo de dominio, reglas de negocio, casos de uso y guías
├── openspec/            # Especificaciones y cambios (Spec-Driven Development)
├── .agents/skills/      # Skills de OpenSpec instaladas para agentes
├── docker-compose.yml   # PostgreSQL 17 y MinIO S3
└── Makefile             # Comandos centralizados (ver notas de actualización)
```

---

## 2. Stack Tecnológico Real

| Capa | Tecnología Real en Código | Notas Clave |
| :--- | :--- | :--- |
| **Backend** | Java 25 LTS, Spring Boot 3.5.16, Maven (`./mvnw`) | **Spring Web MVC + Virtual Threads** (`spring.threads.virtual.enabled: true`) + **SSE** (`SseEmitter`). |
| **Persistencia y BD** | Spring Data JPA (Hibernate, `ddl-auto: validate`) · PostgreSQL 17 · H2 (tests) | Migraciones con **Flyway** (`V1` users, `V2` auctions, `V3` bids, `V4` images) y `@Version` optimistic locking. |
| **Seguridad y Storage** | Spring Security 6 (BCrypt + JWT `jjwt 0.12.6`), MinIO S3 (`8.5.17`) | Validación de archivos por magic bytes binarios (`FileSignatureSecurityAdapter`). ClamAV fue retirado. |
| **Errores y Eventos** | `@RestControllerAdvice` (`ProblemDetail` RFC 7807) + Domain Events | `AuctionEventBroadcaster` escucha `@TransactionalEventListener(AFTER_COMMIT)` y emite por SSE. |
| **Testing Backend** | JUnit 5, `SpringBootTest`, **Karate 1.5.2** (`karate-junit5`) | Tests de dominio, concurrencia (optimistic locking) y suite E2E API (`src/test/resources/karate/*.feature`). |
| **Frontend** | Angular 21.2 LTS, TypeScript ~5.9 (`strict: true`), **npm** | Standalone, `input()`/`output()`/`model()`, Signals, `@defer`, Reactive Forms + **Zod v4**, Tailwind CSS v4.3, Material 21. |
| **Rendering Frontend** | `@angular/ssr` + Express + `zone.js` | **SSR y prerendering activos** (`provideZoneChangeDetection`). |

---

## 3. Comandos de Desarrollo y Verificación

### Backend (`bidmaster-ws/`)
> Requiere **JDK 25** configurado (`java.version = 25` en `pom.xml`).
```bash
cd bidmaster-ws
./mvnw test                                 # Ejecuta tests unitarios, concurrencia y Karate E2E (perfil "test" con H2)
./mvnw spring-boot:run                      # Arranca API en http://localhost:8080 (perfil "dev")
./mvnw clean install -DskipTests            # Compila y empaqueta sin tests
```
Desde la raíz: `make backend-build`, `make backend-test`, `make backend-run`, `make backend-debug` (puerto `5005`).

### Frontend (`bidmaster-front/`)
```bash
cd bidmaster-front
npm install                                 # Gestor configurado en angular.json: npm
npm start                                   # Dev server Angular en http://localhost:4200
npm run build                               # Build SSR/browser de producción (valida TypeScript, templates y SSR)
npm test                                    # Tests unitarios Karma/Jasmine (usa CHROME_BIN=/usr/bin/chromium --watch=false)
npm run test:watch                          # Tests en modo watch interactivo
```
- **Nota sobre `Makefile`:** `make frontend-run` todavía invoca `bun run start` (cuando el repo ya migró a `npm` y eliminó `bun.lock`) y `make frontend-lint` ejecuta `npm run lint` (script no definido en `package.json`). Usa `npm start` y `npm run build` / `npm test` directamente.

### Infraestructura (`docker-compose.yml`)
```bash
docker compose up -d                        # Levanta PostgreSQL 17 (5432) y MinIO (9000/9001)
docker compose down                         # Detiene contenedores
```

---

## 4. Arquitectura y Reglas del Backend (`bidmaster-ws`)

El paquete raíz es `dev.mostapha.bidmaster` con **Arquitectura Hexagonal (Ports & Adapters)**:

```text
dev.mostapha.bidmaster/
├── domain/
│   ├── model/                  # Entidades puras (auction/, bid/, image/, user/). Sin anotaciones Spring/JPA.
│   └── event/                  # Eventos de dominio inmutables (BidPlacedEvent, AuctionStatusChangedEvent)
├── application/
│   ├── port/in/                # Casos de uso (*UseCase)
│   ├── port/out/               # Puertos de salida (*Repository, ImageStoragePort, SecurityScanPort, DomainEventPublisherPort)
│   └── service/                # Implementaciones @Service transaccionales (@Transactional)
├── adapter/
│   ├── in/web/                 # @RestController MVC + SSE, dto/ (request/, response/), mapper/, exception/GlobalExceptionHandler
│   └── out/
│       ├── persistence/        # *RepositoryAdapter, entity/ (*Entity JPA), repository/ (Jpa*Repository)
│       ├── storage/            # MinioStorageAdapter
│       ├── security/           # FileSignatureSecurityAdapter (magic bytes)
│       └── event/              # SpringDomainEventPublisherAdapter, AuctionEventBroadcaster (SSE)
└── config/                     # SecurityConfig, JwtService, JwtAuthenticationFilter, MinioConfig, DevDataInitializer
```

### Reglas estrictas de Backend
1. **Aislamiento de dominio:** `domain/model/` y `domain/event/` no deben importar clases de `adapter/`, `config/` ni anotaciones JPA/Spring. Usa factory methods `create(...)` y `reconstitute(...)` y mantén las invariantes en la entidad (`Auction.placeBid`, `Auction.publish`, etc.).
2. **Transacciones, concurrencia y eventos:** Usa `@Transactional` en servicios de aplicación, `@Version` en entidades JPA para optimistic locking (`409 Conflict` gestionado en `GlobalExceptionHandler`), y publica eventos de dominio vía `DomainEventPublisherPort` para que se emitan por SSE solo `AFTER_COMMIT`.
3. **Migraciones Flyway y esquema:** `hibernate.ddl-auto=validate` exige coincidencia exacta entre `@Entity` y la BD. Todo cambio de esquema requiere script incremental `V{N}__descripcion.sql` en `src/main/resources/db/migration/`.
4. **Validación y errores RFC 7807:** Valida entradas con Jakarta Bean Validation (`@Valid`) y deja que `GlobalExceptionHandler` formatee errores como `ProblemDetail`.
5. **Testing integral:** Mantén cubiertos los cambios de negocio con tests unitarios (`AuctionDomainTest`), tests de concurrencia si afectan pujas/saldos (`ConcurrencyBidTest`) y escenarios E2E en Karate (`src/test/resources/karate/*.feature`). En perfil `test` (`application-test.properties`), `TestStorageConfig` provee almacenamiento en memoria sin requerir MinIO.

---

## 5. Arquitectura y Reglas del Frontend (`bidmaster-front`)

```text
bidmaster-front/src/app/
├── core/                       # guards/ (authGuard), interceptors/ (authInterceptor), services/ (HTTP + Signals + *.spec.ts)
├── layouts/                    # MainLayoutComponent, DashboardLayoutComponent, HeaderComponent
├── pages/                      # Páginas lazy-loaded (auth/, dashboard/, home/, auction-detail/)
├── components/                 # Componentes específicos de página/sección
└── shared/                     # components/, directives/, models/, validators/ (esquemas Zod v4)
```

### Aliases TypeScript (`bidmaster-front/tsconfig.json`)
Importar usando los aliases configurados:
`@core/*`, `@shared/*`, `@components/*`, `@pages/*`, `@layouts/*`, `@environments/*`.

### Reglas estrictas de Frontend
1. **Angular 21 moderno:** Usa componentes standalone, signal APIs (`input()`, `input.required()`, `output()`, `model()`), control flow nativo (`@if`, `@for` con `track`, `@defer`), `inject()` para DI y `signal()`/`computed()` para estado compartido.
2. **Enrutamiento y páginas:** Los componentes en `pages/` usan `export default class ...` para `loadComponent: () => import(...)` en `app.routes.ts`.
3. **Plantillas y estilos:** Separa `.component.ts`, `.component.html` y `.component.css` en componentes no triviales. Usa Tailwind CSS v4 (`@tailwindcss/postcss`) con enfoque mobile-first.
4. **Formularios y validación:** Reactive Forms validados con esquemas **Zod v4** (`shared/validators/`).
5. **Compatibilidad SSR obligatoria:** El proyecto compila con prerender y SSR. Todo acceso a `window`, `document` o `localStorage` **debe** estar protegido con `isPlatformBrowser(inject(PLATFORM_ID))`.

---

## 6. Notas de Estado del Código y Archivos Heredados

Ten en cuenta estos detalles antes de desplegar o modificar configuración:

- **Seguridad HTTP (`SecurityConfig.java`):** Tiene JWT stateless (`JwtAuthenticationFilter`) y BCrypt activos, pero mantiene `.requestMatchers("/api/**").permitAll()` por compatibilidad en desarrollo; los controladores usan `resolveCurrentUser(principal, ...)` con fallback cuando `Principal` es `null`. Al endurecer rutas autenticadas, verifica los tests Karate y el flujo del frontend.
- **Dockerfiles desactualizados respecto al código:** `bidmaster-ws/Dockerfile` todavía usa `eclipse-temurin:17-jdk-alpine` (mientras `pom.xml` compila con Java 25) y `bidmaster-front/Dockerfile` aún referencia `oven/bun:1` y `bun.lock` (eliminado).
- **Documentación de estrategia desactualizada:** `docs/dev-strategy/springboot_best_practices.md` y `README.md` todavía mencionan herramientas anteriores (como Bun o Kafka); valida siempre contra `pom.xml`, `package.json` y `docker-compose.yml`.

---

## 7. Dominio y Documentación de Referencia

Antes de implementar o modificar reglas de negocio, consulta:
- Modelo de dominio e invariantes: [`docs/architecture/domain-model.md`](docs/architecture/domain-model.md)
- Entidades, reglas y estados: [`docs/architecture/domain/`](docs/architecture/domain/README.md)
- Gestión de imágenes: [`docs/architecture/image-management.md`](docs/architecture/image-management.md) y [`docs/api/image-service.md`](docs/api/image-service.md)
- Casos de uso: [`docs/use-cases.md`](docs/use-cases.md)

---

## 8. Flujo de Trabajo con OpenSpec (SDD)

Este repositorio usa **OpenSpec v2** (`openspec/config.yaml` + skills en `.agents/skills/`).

- **Explorar sin cambios:** `/openspec-explore` para investigar requisitos o arquitectura antes de planificar.
- **Proponer un cambio:** `/openspec-propose <nombre-kebab-case>` genera `proposal.md`, `specs/<capacidad>/spec.md`, `design.md` y `tasks.md` dentro de `openspec/changes/<nombre>/`. **No implementar código durante la fase de propuesta.**
- **Implementar:** `/openspec-apply-change` ejecuta las tareas definidas verificando contratos frontend/backend y tests.
- **Sincronizar y archivar:** `/openspec-sync-specs` y `/openspec-archive-change` consolidan las especificaciones en `openspec/specs/` tras completar el cambio.
- **Comandos CLI útiles:** `openspec list`, `openspec status --change <nombre>`, `openspec validate`, `openspec context`, `openspec doctor`.

---

## 9. Criterios de Calidad antes de Finalizar una Tarea

1. **Backend modificado:** ejecutar `cd bidmaster-ws && ./mvnw test` (incluye dominio, concurrencia y Karate E2E) con JDK 25 y comprobar el respeto de la arquitectura hexagonal.
2. **Frontend modificado:** ejecutar `cd bidmaster-front && npm run build` (valida TypeScript estricto, templates Angular y bundle SSR) y `npm test` cuando aplique.
3. **Cambio de contrato API o dominio:** verificar alineación exacta entre DTOs Java (`adapter/in/web/dto/`), `ProblemDetail`, eventos SSE, modelos TypeScript (`shared/models/`), esquemas Zod (`shared/validators/`), migraciones Flyway y documentación en `docs/architecture/` u `openspec/specs/`.
