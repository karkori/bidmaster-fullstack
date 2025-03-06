# 🗂 Categoría (Category)

## 📝 Descripción
Representa una categoría para clasificar productos y organizar las subastas.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| name | String | Nombre de la categoría | 2-50 caracteres |
| slug | String | URL amigable | Único, autogenerado |
| description | String | Descripción | Max 500 caracteres |

### Jerarquía
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| parent | Category | Categoría padre | Opcional |
| level | Integer | Nivel en la jerarquía | 0-3 |
| path | String | Ruta completa | Autogenerado |
| order | Integer | Orden de visualización | ≥ 0 |

### Presentación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| icon | String | Icono representativo | Opcional |
| image | Image | Imagen de categoría | Opcional |
| featured | Boolean | Categoría destacada | Default false |
| visible | Boolean | Visible en navegación | Default true |

### Estadísticas
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| productCount | Integer | Número de productos | ≥ 0 |
| activeAuctionCount | Integer | Subastas activas | ≥ 0 |
| totalAuctionCount | Integer | Total de subastas | ≥ 0 |

## 🔄 Relaciones

### Principales
- **Categoría Padre**: ManyToOne → Category
- **Subcategorías**: OneToMany → Category
- **Productos**: OneToMany → Product
- **Subastas**: OneToMany → Auction

## 🛡️ Invariantes
1. No puede haber ciclos en la jerarquía
2. El nivel máximo de profundidad es 3
3. La ruta debe reflejar la jerarquía completa
4. Solo categorías de último nivel pueden tener productos
5. Los contadores deben mantenerse consistentes

## 📊 Value Objects

### Image
```typescript
interface Image {
    id: string;
    url: string;
    thumbnailUrl: string;
    altText: string;
}
```

### CategoryPath
```typescript
interface CategoryPath {
    segments: string[];
    toString(): string;
    includes(categoryId: string): boolean;
}
```

### CategoryStatistics
```typescript
interface CategoryStatistics {
    productCount: number;
    activeAuctionCount: number;
    totalAuctionCount: number;
    averagePrice: Decimal;
    successRate: number;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Category {
    // Jerarquía
    addSubcategory(category: Category): boolean;
    removeSubcategory(category: Category): boolean;
    moveTo(newParent: Category): boolean;
    
    // Validaciones
    canHaveProducts(): boolean;
    canHaveSubcategories(): boolean;
    isAncestorOf(category: Category): boolean;
    
    // Estadísticas
    updateCounts(): void;
    calculateStatistics(): CategoryStatistics;
    
    // Presentación
    toggleFeatured(): void;
    toggleVisibility(): void;
    updateOrder(newOrder: number): void;
}
```

## 🔍 Consultas Comunes
1. Obtener árbol de categorías
2. Listar categorías destacadas
3. Buscar por nombre
4. Obtener ruta completa
5. Listar subcategorías
6. Obtener categorías con más subastas
7. Obtener categorías por nivel
