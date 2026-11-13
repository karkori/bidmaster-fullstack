# Spec Delta

## Purpose

Definir el flujo de creación y publicación de un producto en subasta alineado con las invariantes del dominio `Auction`, la carga integrada de imágenes, y la correcta visualización de estados e imágenes en tarjetas y detalle.

## ADDED Requirements

### Requirement: Creación de subasta alineada con el contrato de dominio y publicación opcional inmediata

El formulario `CreateAuctionComponent` MUST validar las reglas del dominio `Auction` antes de enviar, reflejar los campos reales de `CreateAuctionRequestDTO` y permitir guardar como borrador (`DRAFT`) o crear y publicar inmediatamente (`ACTIVE`).

#### Scenario: Validación en cliente alineada con dominio y esquema Zod
- **GIVEN** un vendedor autenticado en `/dashboard/create-auction`
- **WHEN** introduce un título menor a 5 o mayor a 100 caracteres, descripción menor a 20 o mayor a 1000 caracteres, `initialPrice <= 0`, `depositRequired < 0` o `endDate` anterior a `ahora + 1 hora`
- **THEN** el formulario marca el campo como inválido con mensaje descriptivo en español y deshabilita el envío hasta cumplir las restricciones

#### Scenario: Crear subasta como borrador con imágenes adjuntas
- **GIVEN** un formulario válido con 2 imágenes seleccionadas (imagen 2 marcada como principal)
- **WHEN** el usuario pulsa **"Guardar borrador"**
- **THEN** el frontend ejecuta `POST /api/auctions` (creando la subasta en `DRAFT`), sube las imágenes mediante `POST /api/images` con `referenceId=<auctionId>`, `isPrimary` y `displayOrder` correspondientes, NO invoca `/publish`, y navega a `/auctions/<slugOrId>`

#### Scenario: Crear y publicar subasta inmediatamente
- **GIVEN** un formulario válido con `depositRequired` y `allowPause` configurados y al menos una imagen seleccionada
- **WHEN** el usuario pulsa **"Publicar subasta"**
- **THEN** el frontend crea la subasta (`POST /api/auctions`), sube las imágenes asociadas (`POST /api/images`) y ejecuta `POST /api/auctions/{id}/publish` (transición `DRAFT -> ACTIVE` emitiendo `AuctionStatusChangedEvent`), navegando a `/auctions/<slugOrId>` donde la subasta aparece activa y visible en el listado público

#### Scenario: Fallo parcial en subida de imágenes tras crear la subasta
- **GIVEN** que `POST /api/auctions` devolvió `201 Created` pero una subida de imagen falla con `400`/`500`
- **WHEN** el flujo captura el error de subida de imagen
- **THEN** la subasta creada NO se pierde ni se duplica si el usuario reintenta, se informa claramente del error al usuario indicando si la subasta quedó en borrador y se desbloquea el estado de envío (`isSubmitting = false`)

### Requirement: Visualización consistente de estado, portada y galería básica de producto

Las tarjetas de subasta (`AuctionCardComponent`) y la vista de detalle (`AuctionDetailComponent`) MUST mostrar estados reales de `AuctionStatus`, resolver la portada con fallback válido y permitir inspeccionar múltiples imágenes.

#### Scenario: Renderizado de badge de estado real y portada con fallback en `AuctionCardComponent`
- **GIVEN** subastas con estados `ACTIVE`, `DRAFT`, `PAUSED`, `FINISHED` o `CANCELLED` y distintas configuraciones de `images`
- **WHEN** se renderiza `AuctionCardComponent`
- **THEN** muestra el badge correcto para cada estado (mapeando `FINISHED` a "Finalizada", `PAUSED` a "Pausada", `CANCELLED` a "Cancelada", `DRAFT` a "Borrador", `ACTIVE` a "Activa"), selecciona la imagen con `primary === true`, si no existe usa `images[0].url` y, si la lista está vacía, usa el placeholder local existente (`/no-image.svg`)

#### Scenario: Cambio de imagen activa mediante miniaturas en `AuctionDetailComponent`
- **GIVEN** una subasta cargada en `/auctions/:idOrSlug` con múltiples imágenes en `auction.images` y estado `ACTIVE` o `FINISHED`
- **WHEN** el usuario hace clic en una miniatura de la galería
- **THEN** la imagen principal mostrada cambia a la seleccionada sin recargar la página y el estado `FINISHED` muestra el bloque informativo de subasta finalizada (y ganador si existe)
