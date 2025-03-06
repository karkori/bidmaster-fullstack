# 📦 Reglas de Productos

## 📝 Reglas Generales

### Creación
1. Información Básica
   - Título descriptivo (10-100 chars)
   - Descripción detallada
   - Categoría principal
   - Subcategorías (max 3)

2. Media
   - Mínimo 3 imágenes
   - Primera imagen destacada
   - Calidad mínima 800x800
   - Máximo 10 imágenes

3. Precios
   - Valor de mercado
   - Precio mínimo
   - Precio sugerido
   - Incrementos recomendados

### Validación
1. Contenido
   - No contenido ofensivo
   - Información precisa
   - Sin datos personales
   - Links permitidos limitados

2. Calidad
   - Fotos claras
   - Descripción completa
   - Especificaciones técnicas
   - Estado del producto

## 🛡️ Validaciones Técnicas

### Producto
```typescript
interface ProductValidation {
    validateBasicInfo(): boolean;
    checkMedia(): boolean;
    validatePricing(): boolean;
    verifyCategory(): boolean;
    checkRestrictions(): boolean;
}
```

### Media
```typescript
interface MediaValidation {
    checkImageQuality(): boolean;
    validateFileTypes(): boolean;
    scanForInappropriate(): boolean;
    verifyDimensions(): boolean;
}
```

## 🎯 Categorización

### Jerarquía
1. Categoría Principal
   - Única
   - Obligatoria
   - De lista predefinida
   - Con metadata específica

2. Subcategorías
   - Máximo 3
   - Relacionadas
   - Coherentes
   - Indexables

### Atributos
```typescript
interface CategoryAttributes {
    required: string[];
    optional: string[];
    specific: Record<string, any>;
    validation: Record<string, any>;
}
```

## 📊 Precios y Valores

### Cálculos
```typescript
interface PriceCalculations {
    marketValue: number;
    minPrice = marketValue * 0.5;
    suggestedPrice = marketValue * 0.8;
    maxPrice = marketValue * 1.5;
    
    calculateIncrement(): number[] {
        return [
            marketValue * 0.01,
            marketValue * 0.02,
            marketValue * 0.05
        ];
    }
}
```

### Restricciones
1. Por Categoría
   - Rango permitido
   - Incrementos mínimos
   - Valores máximos
   - Depósitos requeridos

2. Por Estado
   - Nuevo vs Usado
   - Coleccionable
   - Edición limitada
   - Vintage

## 🔄 Estados de Producto

### Ciclo de Vida
```
[Borrador] → [En Revisión] → [Activo]
     ↓             ↓            ↓
[Descartado]  [Rechazado]   [Pausado] → [Vendido]
```

### Transiciones
1. Borrador → Revisión
   - Info completa
   - Media válida
   - Categoría correcta
   - Precio válido

2. Revisión → Activo
   - Moderación aprobada
   - Vendedor verificado
   - Sin restricciones
   - Documentación OK

## 🚫 Restricciones

### Productos Prohibidos
1. Categorías
   - Ilegales
   - Peligrosos
   - Falsificados
   - Restringidos

2. Condiciones
   - Dañados
   - Incompletos
   - Sin garantía
   - Sin procedencia

### Límites
1. Por Usuario
   - Máximo 50 activos
   - 10 por categoría
   - 5 destacados
   - 3 premium

2. Por Producto
   - 10 imágenes
   - 5000 chars descripción
   - 20 tags
   - 3 subcategorías

## 📱 Notificaciones

### Al Vendedor
1. Creación
   - Confirmación
   - Sugerencias
   - Mejoras
   - Estado

2. Moderación
   - Aprobación
   - Rechazo
   - Cambios requeridos
   - Avisos

### Al Sistema
1. Revisión
   - Nuevos productos
   - Modificaciones
   - Reportes
   - Duplicados

2. Alertas
   - Contenido inadecuado
   - Precios anómalos
   - Patrones sospechosos
   - Infracciones

## 🔍 SEO y Visibilidad

### Optimización
1. Títulos
   - Keywords relevantes
   - Estructura clara
   - Sin repetición
   - Longitud óptima

2. Descripción
   - Formato rico
   - Puntos clave
   - Especificaciones
   - FAQs incluidas

### Metadata
```typescript
interface ProductMetadata {
    seoTitle: string;
    description: string;
    keywords: string[];
    schema: Record<string, any>;
    
    generateRichSnippets(): string {
        // Generación de rich snippets
    }
}
```

## 📊 Analytics

### Métricas
1. Rendimiento
   - Vistas
   - Interés
   - Conversión
   - Tiempo activo

2. Calidad
   - Score de completitud
   - Rating de fotos
   - Precisión de datos
   - Relevancia

### Reportes
1. Por Producto
   - Performance
   - Interacciones
   - Comparativas
   - Tendencias

2. Agregados
   - Categorías top
   - Precios promedio
   - Tasas de éxito
   - Patrones
