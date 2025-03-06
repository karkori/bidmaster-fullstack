# 📦 Producto (Product)

## 📝 Descripción
Representa el artículo que se subasta en la plataforma.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| name | String | Nombre del producto | 3-100 caracteres |
| slug | String | URL amigable | Único, autogenerado |
| sku | String | Código único de producto | Único, opcional |

### Descripción
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| description | String | Descripción detallada | 20-2000 caracteres |
| condition | Enum | Estado del producto | NEW, LIKE_NEW, GOOD, FAIR |
| features | Map<String, String> | Características | Max 20 pares |
| tags | Set<String> | Etiquetas para búsqueda | Max 10 tags |

### Multimedia
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| images | List<Image> | Imágenes del producto | 1-10 imágenes |
| mainImage | Image | Imagen principal | No nulo |
| videos | List<Video> | Videos del producto | Max 3 videos |

### Categorización
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| category | Category | Categoría principal | No nulo |
| subcategories | Set<Category> | Subcategorías | Opcional |
| brand | String | Marca | Opcional |
| model | String | Modelo | Opcional |

## 🔄 Relaciones

### Principales
- **Categoría**: ManyToOne → Category
- **Subcategorías**: ManyToMany → Category
- **Vendedor**: ManyToOne → User
- **Subasta**: OneToOne → Auction

### Secundarias
- **Reportes**: OneToMany → Report
- **Valoraciones**: OneToMany → Rating

## 🛡️ Invariantes
1. Debe tener al menos una imagen
2. La imagen principal debe estar entre las imágenes del producto
3. Las subcategorías deben pertenecer a la categoría principal
4. No puede modificarse si tiene una subasta activa
5. El SKU debe ser único si está definido

## 📊 Value Objects

### Image
```typescript
interface Image {
    id: string;
    url: string;
    thumbnailUrl: string;
    order: number;
    mainImage: boolean;
}
```

### Video
```typescript
interface Video {
    id: string;
    url: string;
    thumbnailUrl: string;
    duration: number;
}
```

### ProductCondition
```typescript
enum ProductCondition {
    NEW,
    LIKE_NEW,
    GOOD,
    FAIR
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Product {
    // Gestión de Imágenes
    addImage(image: Image): boolean;
    removeImage(imageId: string): boolean;
    setMainImage(imageId: string): boolean;
    reorderImages(order: string[]): void;
    
    // Categorización
    setCategory(category: Category): void;
    addSubcategory(category: Category): boolean;
    removeSubcategory(category: Category): boolean;
    
    // Validaciones
    isComplete(): boolean;
    canBeAuctioned(): boolean;
    validateFeatures(): boolean;
    
    // Búsqueda
    matchesSearch(query: string): boolean;
    matchesFilters(filters: ProductFilters): boolean;
}
```

## 🔍 Consultas Comunes
1. Buscar por nombre/descripción
2. Filtrar por categoría
3. Filtrar por condición
4. Buscar por rango de precio
5. Buscar por tags
6. Listar por vendedor
