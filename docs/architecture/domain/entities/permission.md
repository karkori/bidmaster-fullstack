# 🔒 Permiso (Permission)

## 📝 Descripción
Define los permisos y capacidades de acceso en el sistema.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| name | String | Nombre del permiso | No nulo, único |
| code | String | Código técnico | No nulo, único |

### Configuración
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| resource | String | Recurso protegido | No nulo |
| action | String | Acción permitida | No nulo |
| description | String | Descripción detallada | No nulo |
| scope | String | Ámbito de aplicación | No nulo |

### Restricciones
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| conditions | JSON | Condiciones de aplicación | Opcional |
| constraints | JSON | Restricciones adicionales | Opcional |
| priority | Integer | Prioridad de evaluación | ≥ 0 |
| system | Boolean | Permiso de sistema | Default false |

## 🔄 Relaciones

### Principales
- **Roles**: ManyToMany → Role
- **Usuarios**: ManyToMany → User
- **Grupos**: ManyToMany → Group

## 🛡️ Invariantes
1. Los permisos de sistema no pueden modificarse
2. La combinación resource+action debe ser única
3. Las condiciones deben ser válidas
4. Los permisos heredados no pueden contradecir a los superiores
5. Debe mantenerse la jerarquía de permisos

## 📊 Value Objects

### PermissionScope
```typescript
enum PermissionScope {
    GLOBAL,    // Todo el sistema
    TENANT,    // Por inquilino
    GROUP,     // Por grupo
    PERSONAL   // Personal
}
```

### ResourceAction
```typescript
interface ResourceAction {
    resource: string;
    action: 'CREATE' | 'READ' | 'UPDATE' | 'DELETE' | 'EXECUTE';
    constraints?: Record<string, any>;
}
```

### Condition
```typescript
interface Condition {
    field: string;
    operator: 'EQ' | 'NEQ' | 'GT' | 'LT' | 'IN' | 'CONTAINS';
    value: any;
    type: 'AND' | 'OR';
}
```

### PermissionGrant
```typescript
interface PermissionGrant {
    grantee: string;
    granteeType: 'USER' | 'ROLE' | 'GROUP';
    expiresAt?: DateTime;
    grantedBy: User;
    grantedAt: DateTime;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Permission {
    // Validación
    isGranted(user: User, context: Context): boolean;
    evaluateConditions(context: Context): boolean;
    checkConstraints(resource: any): boolean;
    
    // Gestión
    grant(grantee: any, type: string): void;
    revoke(grantee: any, type: string): void;
    
    // Herencia
    inherit(parent: Permission): void;
    override(child: Permission): void;
    
    // Utilidades
    toString(): string;
    toJSON(): Record<string, any>;
}
```

## 🔍 Consultas Comunes
1. Buscar por recurso
2. Filtrar por acción
3. Listar por rol
4. Verificar acceso
5. Obtener permisos efectivos
6. Buscar conflictos
7. Listar permisos heredados
