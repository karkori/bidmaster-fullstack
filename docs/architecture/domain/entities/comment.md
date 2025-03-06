# 💬 Comentario (Comment)

## 📝 Descripción
Gestiona las preguntas, respuestas y discusiones en las subastas.

## 🔑 Atributos

### Identificación
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| id | UUID | Identificador único | No nulo, autogenerado |
| type | Enum | Tipo de comentario | QUESTION, ANSWER, COMMENT |
| auction | Auction | Subasta relacionada | No nulo |

### Contenido
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| content | String | Contenido del comentario | 1-1000 caracteres |
| attachments | List<String> | URLs de adjuntos | Max 5 |
| edited | Boolean | Si fue editado | Default false |
| originalContent | String | Contenido original | Si fue editado |

### Estructura
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| parent | Comment | Comentario padre | Opcional |
| level | Integer | Nivel de anidación | 0-2 |
| path | String | Ruta jerárquica | Autogenerado |
| order | Integer | Orden de visualización | ≥ 0 |

### Estado
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| status | Enum | Estado del comentario | Ver CommentStatus |
| createdAt | DateTime | Fecha de creación | No nulo |
| updatedAt | DateTime | Última actualización | No nulo |
| deletedAt | DateTime | Fecha de eliminación | Opcional |

### Interacción
| Atributo | Tipo | Descripción | Validaciones |
|----------|------|-------------|--------------|
| author | User | Autor del comentario | No nulo |
| likes | Set<User> | Usuarios que dieron like | Opcional |
| flags | Integer | Número de reportes | ≥ 0 |

## 🔄 Relaciones

### Principales
- **Subasta**: ManyToOne → Auction
- **Autor**: ManyToOne → User
- **Padre**: ManyToOne → Comment
- **Respuestas**: OneToMany → Comment
- **Reportes**: OneToMany → Report

## 🛡️ Invariantes
1. Solo el autor puede editar su comentario
2. No se puede editar después de tener respuestas
3. El nivel máximo de anidación es 2
4. Las respuestas deben ser del mismo tipo o ANSWER
5. Un usuario no puede dar like más de una vez

## 📊 Value Objects

### CommentType
```typescript
enum CommentType {
    QUESTION,   // Pregunta sobre la subasta
    ANSWER,     // Respuesta a una pregunta
    COMMENT     // Comentario general
}
```

### CommentStatus
```typescript
enum CommentStatus {
    ACTIVE,     // Visible
    HIDDEN,     // Oculto por moderador
    DELETED,    // Eliminado por autor
    FLAGGED     // Marcado para revisión
}
```

### Attachment
```typescript
interface Attachment {
    url: string;
    type: 'IMAGE' | 'DOCUMENT';
    size: number;
    name: string;
}
```

### CommentMetrics
```typescript
interface CommentMetrics {
    replies: number;
    likes: number;
    flags: number;
    lastActivity: DateTime;
}
```

## 🎯 Comportamiento

### Métodos Principales
```typescript
interface Comment {
    // Gestión
    edit(newContent: string): void;
    delete(): void;
    hide(reason: string): void;
    restore(): void;
    
    // Interacción
    addReply(reply: Comment): void;
    toggleLike(user: User): void;
    flag(reason: string): void;
    
    // Validaciones
    canBeEdited(): boolean;
    canBeDeleted(): boolean;
    isVisible(): boolean;
    
    // Utilidades
    getThread(): Comment[];
    getRootComment(): Comment;
    getMetrics(): CommentMetrics;
}
```

## 🔍 Consultas Comunes
1. Listar por subasta
2. Filtrar por tipo
3. Buscar por autor
4. Obtener hilo completo
5. Listar más populares
6. Buscar por contenido
7. Obtener preguntas sin respuesta
8. Listar comentarios flaggeados
