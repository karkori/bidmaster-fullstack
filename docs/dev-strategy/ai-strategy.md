# 🧠 Estrategia de Desarrollo con IA

## 🎯 Introducción
Este documento describe la estrategia utilizada para desarrollar este proyecto con asistencia de inteligencia artificial, con el objetivo de servir como framework para futuros proyectos.

## 🛠️ Metodología

### 1. **Definición de Objetivos**
- **Prompt Ejemplo**: "Define los objetivos principales del proyecto [Nombre del Proyecto], incluyendo funcionalidades clave, usuarios objetivo y métricas de éxito."

### 2. **Arquitectura del Proyecto**
- **Prompt Ejemplo**: "Crea una estructura de carpetas inicial para un proyecto [Tipo de Proyecto] que incluya [Tecnologías Clave]."
- **Documentación de Estructura**: "Utiliza el comando `tree` para generar y mantener actualizada la estructura de directorios del proyecto, excluyendo directorios innecesarios como node_modules, .git, etc."

```bash
tree -I "node_modules|.git|.angular|.idea|dist|.vscode|target|build" -L 15 /home/mustafa/CascadeProjects/fullstack-demo > /home/mustafa/CascadeProjects/fullstack-demo/docs/dev-strategy/tree_raw.md
```

### 3. **Documentación de Dominio**
- **Prompt Ejemplo**: "Genera la documentación inicial para las entidades, reglas y estados del dominio [Nombre del Dominio]."

### 4. **Implementación de Código**
- **Prompt Ejemplo**: "Implementa la clase [Nombre de la Clase] en [Lenguaje] que cumpla con los siguientes requisitos: [Lista de Requisitos]."

### 5. **Revisión y Refinamiento**
- **Prompt Ejemplo**: "Revisa el siguiente código y sugiere mejoras de optimización, legibilidad y buenas prácticas: [Código]."

## 🔍 Ejemplos de Prompts

### Para Crear Entidades
```
Crea la documentación de la entidad [Nombre de la Entidad] incluyendo atributos, relaciones y comportamientos principales.
```

### Para Definir Reglas de Negocio
```
Documenta las reglas de negocio para [Nombre del Dominio], incluyendo validaciones, restricciones y acciones permitidas.
```

### Para Generar Código
```
Implementa el endpoint [Nombre del Endpoint] en [Lenguaje/Framework] que cumpla con la siguiente especificación: [Detalles].
```

## 📚 Framework para Nuevos Proyectos

### 1. **Inicialización**
- Definir objetivos y alcance
- Crear estructura de carpetas
- Documentar estructura con `tree` para referencia

### 2. **Diseño**
- Documentar dominio
- Definir arquitectura

### 3. **Implementación**
- Generar código base
- Implementar funcionalidades

### 4. **Revisión**
- Optimizar código
- Documentar decisiones

## 💡 Mejores Prácticas

### Para Prompts Efectivos
- Ser específico y claro
- Proporcionar contexto
- Definir el formato esperado

### Para Estructura y Organización de Código
- **Templates separados**: Siempre implementar los templates Angular en archivos HTML separados (`.component.html`) en lugar de templates inline
- **Arquitectura por capas**: Seguir una clara separación de responsabilidades entre componentes, servicios y modelos
- **Componentes standalone**: Preferir componentes Angular standalone para mejorar la modularidad y los tiempos de compilación
- **Aliases de módulos**: Utilizar los aliases definidos en tsconfig.json para mejorar la legibilidad y mantenimiento de las importaciones:
  ```typescript
  // Aliases configurados en tsconfig.json
  "@components/*" : ["app/components/*"]
  "@pages/*" : ["app/pages/*"]
  "@layouts/*" : ["app/layouts/*"]
  "@core/*" : ["app/core/*"]
  "@shared/*" : ["app/shared/*"]
  "@environments/*" : ["environments/*"]
  ```

  Ejemplo de uso:
  ```typescript
  // En lugar de usar rutas relativas complicadas
  import { SomeComponent } from '../../../shared/components/some.component';
  
  // Usar aliases para importaciones más limpias y mantenibles
  import { SomeComponent } from '@shared/components/some.component';
  ```

### Para Documentación de Estructura
- Mantener actualizada la estructura del proyecto con `tree` para mejorar el contexto
- Excluir directorios irrelevantes (node_modules, .git, etc.) para mayor claridad
- Separar las estructuras de frontend y backend para facilitar la comprensión
- Documentar componentes pendientes de implementar para seguimiento

### Para Colaboración con IA
- Revisar y validar resultados
- Iterar y refinar
- Documentar aprendizajes

## 🔗 Recursos

### Plantillas de Prompts
- [Plantilla para Entidades](templates/entity-prompt.md)
- [Plantilla para Reglas](templates/rule-prompt.md)
- [Plantilla para Código](templates/code-prompt.md)

### Ejemplos
- [Ejemplo de Proyecto](examples/sample-project.md)
- [Flujo de Trabajo](examples/workflow.md)
