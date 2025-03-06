# 🏷️ Etiqueta (Tag)

## 📝 Descripción
Sistema de etiquetado flexible para mejorar la búsqueda y categorización de productos y subastas.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| name | String | Nombre de la etiqueta | 2-50 caracteres, único |
| slug | String | Versión URL-friendly | Autogenerado, único |

### Metadatos
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| type | Enum | Tipo de etiqueta | Ver TagType |
| description | String | Descripción corta | Max 200 caracteres |
| icon | String | Icono representativo | Opcional |
| color | String | Color hexadecimal | Formato válido |

### Estadísticas
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| usageCount | Integer | Número de usos | ≥ 0 |
| productCount | Integer | Productos asociados | ≥ 0 |
| auctionCount | Integer | Subastas asociadas | ≥ 0 |
| score | Float | Relevancia calculada | 0-1 |

### Control
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| featured | Boolean | Etiqueta destacada | Default false |
| verified | Boolean | Verificada por admin | Default false |
| active | Boolean | Estado activo | Default true |
| createdAt | DateTime | Fecha de creación | No nulo |

## 🔄 Relaciones

### Principales
- **Productos**: ManyToMany → Product
- **Subastas**: ManyToMany → Auction
- **Categorías**: ManyToMany → Category

### Secundarias
- **Sinónimos**: ManyToMany → Tag
- **Creador**: ManyToOne → User

## 🛡️ Invariantes
1. El nombre debe ser único (case-insensitive)
2. Los sinónimos no pueden formar ciclos
3. Una etiqueta no puede ser sinónimo de sí misma
4. Los contadores deben mantenerse consistentes
5. Las etiquetas verificadas solo pueden ser modificadas por admins

## 📊 Value Objects

### TagType
```typescript
enum TagType {
    PRODUCT_FEATURE,    // Característica del producto
    BRAND,             // Marca
    STYLE,             // Estilo/Diseño
    CONDITION,         // Estado del producto
    MATERIAL,          // Material
    OCCASION,          // Ocasión de uso
    TREND,             // Tendencia
    CUSTOM             // Personalizada
}
```

### TagStatistics
```typescript
interface TagStatistics {
    totalUses: number;
    productUses: number;
    auctionUses: number;
    searchCount: number;
    clickCount: number;
    conversionRate: number;
}
```

### TagRelation
```typescript
interface TagRelation {
    type: 'SYNONYM' | 'RELATED' | 'PARENT' | 'CHILD';
    weight: number;
    source: Tag;
    target: Tag;
}
```

### TagMetadata
```typescript
interface TagMetadata {
    seoTitle?: string;
    seoDescription?: string;
    imageUrl?: string;
    customFields: Record<string, any>;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Tag {
    // Gestión
    merge(other: Tag): void;
    split(newName: string): Tag;
    addSynonym(tag: Tag): void;
    verify(): void;
    
    // Validaciones
    canBeMerged(other: Tag): boolean;
    isRelatedTo(tag: Tag): boolean;
    
    // Estadísticas
    updateCounts(): void;
    calculateScore(): number;
    getUsageStats(): TagStatistics;
    
    // Búsqueda
    getSimilarTags(): Tag[];
    getRelatedProducts(): Product[];
    matchesText(query: string): boolean;
}
```

## 🔍 Consultas Comunes
1. Buscar por nombre
2. Filtrar por tipo
3. Listar más usadas
4. Obtener sugerencias
5. Buscar relacionadas
6. Listar por categoría
7. Obtener trending tags
8. Buscar sinónimos

## 🔄 Gestión de Sinónimos

### Reglas
1. Gestión automática de redirecciones
2. Mantenimiento de histórico
3. Propagación de estadísticas
4. Unificación de búsquedas

### Ejemplo
```typescript
// Gestión de sinónimos
laptop ⟷ portátil
zapatillas ⟷ tenis ⟷ deportivas
celular ⟷ móvil ⟷ smartphone
```

## 📊 Análisis y SEO

### Métricas
- Frecuencia de uso
- Tasa de conversión
- Relevancia en búsquedas
- Tendencias temporales

### SEO
- URLs amigables
- Meta descripciones
- Contenido relacionado
- Breadcrumbs
