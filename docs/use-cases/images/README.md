# 🖼️ Casos de Uso: Gestión de Imágenes

## 📋 Visión General

La gestión de imágenes en BidMaster permite a los usuarios subir, visualizar y administrar imágenes asociadas a las subastas. Este documento detalla los casos de uso primarios para esta funcionalidad.

## 🔄 Casos de Uso Principales

### UC-IMG-1: Subida de Imágenes para Subastas

**Actor Principal:** Usuario vendedor

**Precondiciones:**
- El usuario está autenticado en el sistema
- El usuario tiene permisos para crear/editar subastas

**Flujo Principal:**
1. El usuario accede al formulario de creación/edición de subasta
2. El usuario selecciona las imágenes a subir desde su dispositivo
3. El sistema valida cada imagen (formato, tamaño, dimensiones)
4. El sistema realiza un análisis de seguridad (escaneo antivirus)
5. Las imágenes son procesadas y almacenadas en el servicio de almacenamiento
6. El sistema muestra previsualizaciones de las imágenes subidas
7. El sistema asocia las referencias de las imágenes con la subasta

**Flujos Alternativos:**
- Si alguna imagen no pasa la validación, se muestra un mensaje de error específico
- Si el escaneo de seguridad detecta contenido malicioso, la imagen es rechazada
- El usuario puede eliminar imágenes antes de finalizar la creación de la subasta

**Postcondiciones:**
- Las imágenes quedan almacenadas y asociadas a la subasta
- Las imágenes son accesibles mediante URLs seguras

### UC-IMG-2: Visualización de Imágenes de Subastas

**Actor Principal:** Cualquier usuario (autenticado o anónimo)

**Precondiciones:**
- Existen subastas con imágenes asociadas

**Flujo Principal:**
1. El usuario navega a la página de detalles de una subasta
2. El sistema carga y muestra las imágenes asociadas a la subasta
3. El usuario puede navegar entre múltiples imágenes (si existen)
4. El usuario puede ampliar las imágenes para ver más detalles

**Postcondiciones:**
- El usuario visualiza las imágenes sin interrupciones
- El sistema registra métricas de visualización (opcional)

### UC-IMG-3: Administración de Imágenes

**Actor Principal:** Usuario vendedor o administrador

**Precondiciones:**
- El usuario está autenticado
- El usuario es propietario de la subasta o tiene rol de administrador

**Flujo Principal:**
1. El usuario accede a la edición de una subasta existente
2. El sistema muestra las imágenes actuales
3. El usuario puede:
   - Añadir nuevas imágenes
   - Eliminar imágenes existentes
   - Reordenar imágenes
4. El sistema valida y procesa cualquier nueva imagen
5. El sistema actualiza la asociación entre imágenes y subasta

**Flujos Alternativos:**
- Si el usuario elimina todas las imágenes, el sistema muestra una advertencia
- Si la subasta ya está activa, pueden aplicarse restricciones a la eliminación de imágenes

**Postcondiciones:**
- Los cambios en las imágenes quedan reflejados en la subasta
- Las imágenes eliminadas son borradas del almacenamiento

## 🔒 Casos de Uso de Seguridad

### UC-IMG-SEC-1: Validación de Seguridad de Imágenes

**Actor Principal:** Sistema

**Precondiciones:**
- Un usuario ha intentado subir una imagen

**Flujo Principal:**
1. El sistema recibe la imagen para validación
2. El sistema verifica el formato real del archivo (más allá de la extensión)
3. El sistema escanea la imagen con ClamAV en busca de malware
4. El sistema sanitiza metadatos EXIF para proteger la privacidad
5. El sistema genera un nuevo nombre aleatorio para el archivo

**Flujos Alternativos:**
- Si se detecta contenido malicioso, la imagen es rechazada y se notifica al usuario
- Si el formato no coincide con los permitidos, se rechaza la subida

**Postcondiciones:**
- Solo las imágenes seguras son aceptadas en el sistema
- Se mantiene un registro de intentos de subida con problemas de seguridad

## 📊 Requisitos No Funcionales

- **Rendimiento**: El tiempo de procesamiento de imágenes no debe exceder los 3 segundos por imagen
- **Escalabilidad**: El sistema debe manejar hasta 1000 subidas de imágenes simultáneas
- **Disponibilidad**: El servicio de imágenes debe tener una disponibilidad del 99.9%
- **Seguridad**: Todas las imágenes deben pasar por verificación antivirus
- **Privacidad**: Los metadatos EXIF deben ser eliminados para proteger información sensible
- **Integridad**: Las imágenes no deben degradarse significativamente durante el procesamiento

## 🔄 Integración con Otros Módulos

- **Módulo de Subastas**: Las imágenes se asocian a subastas específicas
- **Módulo de Usuarios**: Las imágenes tienen propietarios y permisos de acceso
- **Módulo de Administración**: Los administradores pueden moderar contenido visual inapropiado
