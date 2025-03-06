# 📍 Dirección (Address)

## 📝 Descripción
Gestiona las direcciones de envío y facturación de los usuarios.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de dirección | SHIPPING, BILLING, BOTH |
| name | String | Nombre de referencia | 2-50 caracteres |

### Ubicación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| street | String | Calle y número | No nulo |
| apartment | String | Piso/Departamento | Opcional |
| city | String | Ciudad | No nulo |
| state | String | Estado/Provincia | No nulo |
| country | String | País | ISO 3166-1 |
| zipCode | String | Código postal | Formato válido |

### Contacto
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| contactName | String | Nombre de contacto | No nulo |
| phone | String | Teléfono | Formato válido |
| instructions | String | Instrucciones | Opcional |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| default | Boolean | Dirección por defecto | Por tipo |
| verified | Boolean | Dirección verificada | Default false |
| active | Boolean | Dirección activa | Default true |
| lastUsed | DateTime | Último uso | Opcional |

## 🔄 Relaciones

### Principales
- **Usuario**: ManyToOne → User
- **Envíos**: OneToMany → Shipping
- **Facturas**: OneToMany → Invoice

## 🛡️ Invariantes
1. Solo puede haber una dirección por defecto por tipo
2. El código postal debe corresponder a la ciudad/estado
3. El formato del teléfono debe ser válido para el país
4. Una dirección verificada no puede modificarse
5. Debe haber al menos una dirección activa por usuario

## 📊 Value Objects

### AddressType
```typescript
enum AddressType {
    SHIPPING,   // Dirección de envío
    BILLING,    // Dirección de facturación
    BOTH        // Ambos usos
}
```

### Coordinates
```typescript
interface Coordinates {
    latitude: number;
    longitude: number;
    accuracy: number;
}
```

### AddressValidation
```typescript
interface AddressValidation {
    verified: boolean;
    method: 'API' | 'MANUAL' | 'DELIVERY';
    verifiedAt: DateTime;
    verifiedBy: string;
}
```

### AddressFormat
```typescript
interface AddressFormat {
    format: string;
    required: string[];
    optional: string[];
    regex: Record<string, string>;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Address {
    // Gestión
    setAsDefault(): void;
    verify(): void;
    deactivate(): void;
    update(data: Partial<Address>): void;
    
    // Validaciones
    validate(): boolean;
    isComplete(): boolean;
    canBeModified(): boolean;
    
    // Utilidades
    format(style: string): string;
    getCoordinates(): Promise<Coordinates>;
    calculateDistance(other: Address): number;
    
    // Verificación
    validatePostalCode(): boolean;
    validatePhoneNumber(): boolean;
    verifyWithService(): Promise<boolean>;
}
```

## 🔍 Consultas Comunes
1. Buscar por usuario
2. Filtrar por tipo
3. Obtener dirección por defecto
4. Listar direcciones activas
5. Buscar por ciudad/estado
6. Obtener direcciones verificadas
7. Listar por último uso
8. Validar cobertura de envío

## 🌍 Formatos por País

### España
```typescript
interface SpainAddress {
    street: string;      // Calle
    number: string;      // Número
    floor?: string;      // Piso
    door?: string;       // Puerta
    postalCode: string;  // Código Postal (5 dígitos)
    city: string;        // Ciudad
    province: string;    // Provincia
}
```

### México
```typescript
interface MexicoAddress {
    street: string;      // Calle
    number: string;      // Número
    interior?: string;   // Interior
    colony: string;      // Colonia
    postalCode: string;  // Código Postal
    city: string;        // Ciudad
    state: string;       // Estado
}
```

### Estados Unidos
```typescript
interface USAddress {
    street: string;      // Street
    unit?: string;       // Apt/Suite
    city: string;        // City
    state: string;       // State (2 letters)
    zipCode: string;     // ZIP Code
}
```
