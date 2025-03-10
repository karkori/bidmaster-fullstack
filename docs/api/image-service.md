# 🖼️ API del Servicio de Imágenes

## 📋 Visión General

Esta API proporciona endpoints para la gestión completa de imágenes en la plataforma BidMaster, con un enfoque especial en la seguridad y rendimiento.

## 🔐 Autenticación

Todos los endpoints requieren autenticación mediante token JWT, siguiendo el mismo esquema de autenticación implementado en el resto de BidMaster.

## 🛣️ Endpoints

### Subida de Imágenes

#### `POST /api/images/upload`

Permite subir una o varias imágenes al sistema.

**Formato de Solicitud:**
```
Content-Type: multipart/form-data
Authorization: Bearer {jwt-token}

form-data:
  - images: [archivo1.jpg, archivo2.jpg, ...]
  - type: AUCTION  // Tipo de imagen (AUCTION, PROFILE, etc.)
```

**Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "filename": "imagen1.jpg",
      "contentType": "image/jpeg",
      "size": 24560,
      "url": "/api/images/550e8400-e29b-41d4-a716-446655440000",
      "thumbnailUrl": "/api/images/550e8400-e29b-41d4-a716-446655440000/thumbnail",
      "uploadDate": "2025-03-10T14:30:45.123Z"
    },
    // ...más imágenes
  ]
}
```

**Respuestas de Error:**
- `400 Bad Request`: Formato de archivo inválido o tamaño excesivo
- `403 Forbidden`: Usuario no autorizado
- `413 Payload Too Large`: Tamaño total de archivos excede el límite
- `415 Unsupported Media Type`: Tipo de archivo no soportado
- `422 Unprocessable Entity`: La imagen no pasó la validación de seguridad

### Obtención de Imágenes

#### `GET /api/images/{imageId}`

Recupera la imagen original por su ID.

**Parámetros:**
- `imageId`: Identificador único de la imagen

**Respuesta:**
- `200 OK`: Datos binarios de la imagen con el Content-Type correspondiente
- `404 Not Found`: Imagen no encontrada

#### `GET /api/images/{imageId}/thumbnail`

Recupera la versión miniatura de la imagen.

**Parámetros:**
- `imageId`: Identificador único de la imagen

**Respuesta:**
- `200 OK`: Datos binarios de la miniatura con el Content-Type correspondiente
- `404 Not Found`: Imagen no encontrada

#### `GET /api/images/{imageId}/metadata`

Recupera solo los metadatos de la imagen.

**Respuesta Exitosa (200 OK):**
```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "filename": "original_name.jpg",
  "contentType": "image/jpeg",
  "size": 24560,
  "width": 1200,
  "height": 800,
  "uploadDate": "2025-03-10T14:30:45.123Z",
  "owner": "user123",
  "type": "AUCTION"
}
```

### Eliminación de Imágenes

#### `DELETE /api/images/{imageId}`

Elimina una imagen y sus miniaturas asociadas.

**Parámetros:**
- `imageId`: Identificador único de la imagen

**Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "message": "Imagen eliminada correctamente"
}
```

**Respuestas de Error:**
- `403 Forbidden`: Usuario no autorizado para eliminar esta imagen
- `404 Not Found`: Imagen no encontrada

### Gestión por Lotes

#### `POST /api/images/batch/delete`

Elimina múltiples imágenes en una sola operación.

**Formato de Solicitud:**
```json
{
  "imageIds": [
    "550e8400-e29b-41d4-a716-446655440000",
    "550e8400-e29b-41d4-a716-446655440001",
    // ...más IDs
  ]
}
```

**Respuesta Exitosa (200 OK):**
```json
{
  "success": true,
  "results": {
    "successful": ["id1", "id2"],
    "failed": {
      "id3": "No encontrada",
      "id4": "Sin permiso"
    }
  }
}
```

## 📏 Límites y Restricciones

- **Tipos de archivo permitidos**: JPG, PNG, WEBP, GIF
- **Tamaño máximo por archivo**: 5MB
- **Tamaño máximo total por solicitud**: 20MB
- **Número máximo de archivos por solicitud**: 10
- **Dimensiones máximas**: 4000x4000 píxeles

## 🔄 Integración con Subastas

Cuando una imagen se carga específicamente para una subasta, debe utilizarse el endpoint `/api/images/upload` con `type=AUCTION` y luego asociar el ID de imagen devuelto al crear o actualizar una subasta mediante el endpoint de subastas existente.

### Ejemplo de flujo de trabajo:

1. **Subir imágenes:**
   ```
   POST /api/images/upload
   type=AUCTION
   ```

2. **Crear subasta con imágenes:**
   ```
   POST /api/auctions
   {
     "title": "MacBook Pro 2025",
     "description": "...",
     "categoryId": "electronics",
     "initialPrice": 999.99,
     "endDate": "2025-04-10T12:00:00Z",
     "imageIds": [
       "550e8400-e29b-41d4-a716-446655440000",
       "550e8400-e29b-41d4-a716-446655440001"
     ],
     // ...otros campos
   }
   ```

## 🔍 Consideraciones de Seguridad

- La API implementa validación estricta de formatos y contenidos
- Todas las imágenes son escaneadas con ClamAV antes de ser almacenadas
- Las URLs directas tienen una validez temporal limitada
- Los metadatos EXIF son eliminados automáticamente para proteger la privacidad
