# 🧠 Documentación Principal sobre la Estrategia de Desarrollo del Proyecto BidMaster

## 🎯 Introducción

Este documento sirve como punto central de referencia para el proyecto [ **BidMaster, una plataforma de subastas online** ]. Aquí encontrarás información general sobre la estructura del proyecto, estrategias de desarrollo, y referencias a documentación más específica. Aunque puede servir como documentación al usuario/desarrollador, mi objetivo principal con ella es para guiar a los agente de IA para entender rapidamente el contexto y la aquitectura del proyecto.


## 📑 Documentación del Proyecto

El proyecto cuenta con varios documentos específicos que detallan las mejores prácticas y estándares para cada parte del sistema:

### 📚 Guías Específicas

1. **[Angular Best Practices](angular_best_practices.md)** - Guía para el desarrollo frontend con Angular 19

   - Estructura de componentes y organización del código
   - Componentes reutilizables y formularios
   - Gestión de estado y reactividad con signals
   - Optimización y rendimiento
   - etc.

2. **[Spring Boot Best Practices](springboot_best_practices.md)** - Guía para el desarrollo backend con Spring Boot 3

   - Arquitectura hexagonal (ports and adapters)
   - Estructura de paquetes y organización del código
   - Implementación de casos de uso y repositorios
   - Seguridad y validación

3. **[Estructura del Proyecto](tree.md)** - Estructura actualizada de directorios y archivos
   - Organización de directorios frontend y backend
   - Visualización jerárquica del proyecto

## 🛠️ Estructura del Proyecto

El proyecto sigue una arquitectura full-stack claramente dividida:

### Backend (Spring Boot 3)

- Arquitectura hexagonal (ports and adapters)
- API RESTful para comunicación con el frontend
- Seguridad con JWT
- Base de datos PostgreSQL

### Frontend (Angular 19)

- Componentes standalone
- Sistema modular con lazy loading
- Gestión de estado reactivo con signals
- UI moderna con Tailwind CSS

## 📊 Comandos Útiles

### Makefile

El proyecto incluye un `Makefile` en la raíz que centraliza todos los comandos útiles para desarrollo. Este enfoque facilita tanto a los desarrolladores como a los agentes de IA la ejecución de tareas comunes sin necesidad de recordar comandos específicos para cada tecnología.

```bash
# Ver todos los comandos disponibles con descripciones
make help

# Comandos principales
make setup                  # Configura todo el proyecto (instala dependencias)
make start                  # Inicia toda la aplicación (frontend + backend + DB)
make stop                   # Detiene todos los servicios
make clean                  # Limpia archivos generados y cachés

# Comandos específicos para frontend
make frontend-run           # Inicia el servidor de desarrollo Angular
make frontend-build         # Construye la aplicación Angular para producción

# Comandos específicos para backend
make backend-run            # Inicia el servidor Spring Boot
make backend-debug          # Inicia el servidor en modo debug

# Comandos para Docker
make docker-up              # Inicia contenedores
make docker-down            # Detiene contenedores

# Comandos para documentación
make tree                   # Muestra la estructura del proyecto completa
make update-docs            # Actualiza la documentación de estructura en tree.md

# Comandos específicos para IA
make ai-help                # Muestra información útil para agentes IA
make check-structure        # Muestra la estructura del proyecto
make generate-component     # Genera nuevos componentes con la estructura adecuada
```

### Navegación Rápida

```bash
# Frontend
cd /home/mustafa/CascadeProjects/fullstack-demo/bidmaster-front

# Backend
cd /home/mustafa/CascadeProjects/fullstack-demo/bidmaster-ws

# Documentación de estrategias de desarrollo
cd /home/mustafa/CascadeProjects/fullstack-demo/docs/dev-strategy

# Documentación de arquitectura y dominio
cd /home/mustafa/CascadeProjects/fullstack-demo/docs/architecture
```

## 📚 Documentación de Arquitectura y Dominio

La documentación detallada sobre el modelo de dominio, entidades, reglas de negocio y estados del sistema se encuentra en el directorio de arquitectura:

```
/docs/architecture/
```

### Documentos Principales

- **Modelo de Dominio**: [`/docs/architecture/domain-model.md`](/docs/architecture/domain-model.md)

  - Descripción general de todas las entidades y sus relaciones
  - Invariantes del sistema

- **Entidades**: [`/docs/architecture/domain/entities/`](/docs/architecture/domain/entities/)

  - Documentación detallada de cada entidad del sistema
  - Atributos, comportamientos y validaciones

- **Reglas de Negocio**: [`/docs/architecture/domain/rules/`](/docs/architecture/domain/rules/)

  - Reglas específicas para usuarios, subastas, productos, etc.
  - Políticas de seguridad y administración

- **Estados y Transiciones**: [`/docs/architecture/domain/states/`](/docs/architecture/domain/states/)
  - Diagramas de estado para entidades clave
  - Reglas de transición entre estados

> **Nota**: Al modificar cualquier aspecto del dominio o lógica de negocio, actualizar la documentación correspondiente en estos directorios.

## 📝 Estrategias de Desarrollo

### Prompts Útiles para IA

#### Para Crear/Modificar Funcionalidades

```
Implementa la funcionalidad de [Nombre] que permita [Objetivo] utilizando [Tecnología] y siguiendo las mejores prácticas documentadas en [Referencia a Documento].
```

#### Para Análisis y Mejoras

```
Analiza el código en [Ruta del Archivo] y sugiere mejoras para optimizar [Rendimiento/Seguridad/Mantenibilidad] siguiendo nuestras mejores prácticas.
```

#### Para Debugging

```
Estoy recibiendo el siguiente error: [Mensaje de Error]. El problema ocurre en [Ruta del Archivo] cuando [Descripción del Contexto]. ¿Cuál podría ser la causa y cómo puedo solucionarlo?
```

## 💡 Mejores Prácticas

### Para Prompts Efectivos

- Ser específico y claro
- Proporcionar contexto
- Definir el formato esperado

### Para Estructura y Organización de Código

Para detalles específicos sobre la estructura y organización del código, consultar las guías específicas:

- **Frontend**: Todas las directrices para Angular se encuentran en [Angular Best Practices](angular_best_practices.md)

- **Backend**: Las directrices para Spring Boot están en [Spring Boot Best Practices](springboot_best_practices.md)



### Para Documentación de Estructura

- Mantener actualizada la estructura del proyecto con `tree` para mejorar el contexto
- Excluir directorios irrelevantes (node_modules, .git, etc.) para mayor claridad
- Separar las estructuras de frontend y backend para facilitar la comprensión
- Documentar componentes pendientes de implementar para seguimiento

### Para Colaboración con IA

- Revisar y validar resultados
- Iterar y refinar
- Documentar aprendizajes

## 🚀 Roadmap del Proyecto

### Fase 1: Fundamentos

- ✅ Configuración del proyecto frontend y backend
- ✅ Implementación de autenticación y autorización
- ✅ Estructura básica de la UI con dashboard y layout
- ✅ Implementación de modelos de dominio básicos

### Fase 2: Funcionalidades Core

- ✅ Creación y gestión de subastas
- ✅ Sistema de pujas y ofertas
- ⏳ Notificaciones en tiempo real
- ⏳ Sistema de búsqueda y filtros

### Fase 3: Optimización y Expansión

- ⏳ Mejoras de rendimiento y UX
- ⏳ Sistema de valoraciones y comentarios
- ⏳ Integración con pasarelas de pago
- ⏳ App móvil (fuera del alcance inicial)

## 🔗 Enlaces Útiles

- [Angular.io](https://angular.io/docs) - Documentación oficial de Angular
- [Spring.io](https://spring.io/projects/spring-boot) - Documentación oficial de Spring Boot
- [Tailwind CSS](https://tailwindcss.com/docs) - Documentación oficial de Tailwind CSS
- [JWT.io](https://jwt.io/) - Información sobre JSON Web Tokens
