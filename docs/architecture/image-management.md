# 🖼️ Arquitectura de Gestión de Imágenes

## 📋 Visión General

El subsistema de gestión de imágenes proporciona capacidades seguras y escalables para el almacenamiento y gestión de imágenes de subastas en BidMaster. Se ha diseñado como un servicio independiente con su propia API dedicada, integrado con mecanismos de seguridad avanzados.

## 🏗️ Componentes

### Diagrama de Arquitectura

```
┌─────────────────┐      ┌─────────────────┐      ┌─────────────────┐
│                 │      │                 │      │                 │
│  Cliente Angular├─────►│ API Spring Boot ├─────►│  MinIO Storage  │
│                 │      │                 │      │                 │
└─────────────────┘      └────────┬────────┘      └─────────────────┘
                                  │
                                  ▼
                         ┌─────────────────┐
                         │                 │
                         │  ClamAV Scanner │
                         │                 │
                         └─────────────────┘
```

### 1. Servicio de Almacenamiento (MinIO)

MinIO se utiliza como servidor de almacenamiento de objetos compatible con Amazon S3, proporcionando:

- Almacenamiento fiable y escalable para imágenes
- Compatibilidad con la API de S3 para facilitar la migración a AWS en el futuro
- Sistemas de permisos y políticas para controlar el acceso a los recursos
- Capacidad para generar URLs prefirmadas para acceso temporal a imágenes

### 2. Escáner de Seguridad (ClamAV)

Se integra ClamAV como motor antivirus para analizar todas las imágenes subidas:

- Detección de malware, virus y archivos maliciosos
- API para escaneo de archivos en tiempo real
- Actualizaciones regulares de la base de datos de definiciones
- Sistema de cuarentena para archivos sospechosos

### 3. API de Gestión de Imágenes (Spring Boot)

El servicio Spring Boot implementa la lógica de negocio para:

- Validación de imágenes (formato, tamaño, dimensiones)
- Procesamiento de imágenes (redimensionado, optimización)
- Orquestación del flujo de subida: recepción → escaneo → almacenamiento
- Generación de URLs seguras para el frontend

## 🔀 Flujos de Trabajo

### Flujo de Subida de Imágenes

1. El cliente Angular inicia la subida de una o varias imágenes
2. La API de Spring Boot recibe los archivos y realiza validaciones iniciales
3. Las imágenes se envían a ClamAV para escanear posibles amenazas
4. Si pasan la validación de seguridad, se almacenan en MinIO
5. La API devuelve los identificadores/URLs de las imágenes al cliente
6. El cliente incluye estos identificadores al crear o actualizar una subasta

### Flujo de Recuperación de Imágenes

1. El cliente solicita una imagen mediante su identificador
2. La API genera una URL firmada temporal para acceso directo a MinIO
3. El cliente utiliza esta URL para mostrar la imagen al usuario

## 🔒 Consideraciones de Seguridad

- **Validación de tipos MIME**: Verificación estricta del formato real del archivo
- **Escaneo de malware**: Todas las imágenes son analizadas por ClamAV
- **Generación de nombres aleatorios**: Evita la predicción y colisiones
- **Eliminación de metadatos**: Sanitización de información EXIF y otros metadatos
- **Limitación de tamaño**: Restricción en el tamaño máximo de archivo aceptado
- **URLs temporales**: Las URLs de acceso tienen caducidad para limitar exposición

## 🔄 Escalabilidad y Evolución

La arquitectura propuesta permite:

- **Escalabilidad horizontal**: Todos los componentes pueden escalarse independientemente
- **Migración a AWS**: Al usar interfaces compatibles con S3, la migración a AWS S3 es directa
- **Evolución a Python**: El diseño facilita reemplazar componentes de procesamiento específicos por microservicios Python en el futuro
- **Observabilidad**: Incorporación de métricas y logs para monitoreo de salud del sistema

## 📈 Beneficios

- **Seguridad mejorada**: Protección contra subidas maliciosas
- **Rendimiento optimizado**: Procesamiento eficiente de imágenes
- **Mantenibilidad**: Separación clara de responsabilidades
- **Experiencia de usuario**: Flujo de subida intuitivo con validación inmediata
