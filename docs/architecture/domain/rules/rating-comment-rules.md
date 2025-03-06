# ⭐ Reglas de Valoraciones y Comentarios

## 📝 Reglas Generales

### Valoraciones
1. Solo después de transacción completada
2. Período límite: 30 días post-entrega
3. Una valoración por transacción
4. Modificable hasta 7 días después
5. Incluye rating (1-5) y comentario

### Comentarios
1. Longitud: 10-1000 caracteres
2. No contenido ofensivo/spam
3. Relevante a la transacción
4. Moderación antes de publicación
5. Respuesta única del vendedor

## 🛡️ Validaciones

### Valoraciones
```typescript
interface RatingValidation {
    validateTransaction(): boolean;
    checkTimeWindow(): boolean;
    validateScore(): boolean;
    checkDuplication(): boolean;
    validateContent(): boolean;
}
```

### Comentarios
```typescript
interface CommentValidation {
    checkLength(): boolean;
    validateContent(): boolean;
    checkSpam(): boolean;
    validateUser(): boolean;
    checkModeration(): boolean;
}
```

## 🎯 Sistema de Puntuación

### Cálculo de Rating
```typescript
interface RatingCalculation {
    userScore = weightedAverage([
        lastMonth: 0.5,
        last3Months: 0.3,
        allTime: 0.2
    ]);
    
    productScore = weightedAverage([
        quality: 0.4,
        accuracy: 0.3,
        communication: 0.3
    ]);
}
```

### Impacto
1. Visibilidad en búsquedas
2. Límites de operación
3. Badges y reconocimientos
4. Acceso a características premium

## 📊 Moderación

### Filtros Automáticos
1. Palabras prohibidas
2. Patrones de spam
3. Contenido duplicado
4. Enlaces externos
5. Información personal

### Revisión Manual
1. Contenido flaggeado
2. Valoraciones extremas
3. Reportes de usuarios
4. Patrones sospechosos
5. Apelaciones

## 🔄 Estados

### Valoraciones
```
[Pendiente] → [En Revisión] → [Publicada]
     ↓             ↓             ↓
[Expirada]    [Rechazada]    [Eliminada]
```

### Comentarios
```
[Borrador] → [En Moderación] → [Publicado]
     ↓              ↓             ↓
[Cancelado]     [Rechazado]   [Archivado]
```

## 🚫 Restricciones

### Contenido Prohibido
1. Lenguaje ofensivo
2. Información personal
3. Spam/Publicidad
4. Enlaces externos
5. Amenazas/Acoso

### Límites
1. Por Usuario
   - Max 3 valoraciones/día
   - Max 10 comentarios/día
   - Min 24h entre valoraciones
   - Max 5 ediciones totales

2. Por Item
   - Max 1 valoración/compra
   - Max 1 respuesta/valoración
   - Max 100 comentarios/item
   - Max 3 reportes antes de revisión

## 📱 Notificaciones

### Al Autor
1. Valoración publicada
2. Comentario aprobado
3. Contenido rechazado
4. Respuesta recibida
5. Reporte procesado

### Al Receptor
1. Nueva valoración
2. Nuevo comentario
3. Respuesta requerida
4. Alerta de reporte
5. Recordatorio pendiente

## 🌟 Gamificación

### Badges
1. Top Reviewer
   - 50+ valoraciones
   - 90%+ útiles
   - Contenido detallado
   - Sin rechazos

2. Helpful Commenter
   - 100+ comentarios
   - Alta interacción
   - Respuestas útiles
   - Contribuciones valoradas

### Incentivos
1. Puntos por participación
2. Descuentos especiales
3. Acceso anticipado
4. Badges exclusivos
5. Privilegios especiales

## 📊 Análisis

### Métricas
1. Satisfacción
   - Rating promedio
   - Tendencias
   - Distribución
   - Comparativas

2. Engagement
   - Tasa de respuesta
   - Tiempo promedio
   - Interacciones
   - Utilidad percibida

### Reportes
1. Diarios
   - Nuevas valoraciones
   - Moderación pendiente
   - Reportes activos
   - Tendencias

2. Mensuales
   - Análisis de sentimiento
   - Patrones de uso
   - Áreas de mejora
   - Recomendaciones

## 🔍 SEO y Visibilidad

### Optimización
1. Estructura rica
   - Microdata
   - Schema.org
   - Rich snippets
   - Agregaciones

2. Indexación
   - Prioridad por relevancia
   - Actualización frecuente
   - Filtrado de spam
   - Contenido único
