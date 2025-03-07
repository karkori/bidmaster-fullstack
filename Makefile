# BidMaster Project Makefile
# Facilita las tareas comunes de desarrollo y mantenimiento del proyecto

# Colores para mensajes
BLUE=\033[0;34m
GREEN=\033[0;32m
YELLOW=\033[0;33m
RED=\033[0;31m
NC=\033[0m # No Color

# Rutas principales
FRONTEND_DIR=bidmaster-front
BACKEND_DIR=bidmaster-ws
DOCS_DIR=docs
PROJECT_DIR=$(shell pwd)

# Variables de configuración
PORT_BACKEND=8080
PORT_FRONTEND=4200

# Comando principal para mostrar todos los comandos disponibles con descripciones
.PHONY: help
help:
	@echo "${BLUE}BidMaster Project - Comandos Disponibles${NC}"
	@echo ""
	@echo "${YELLOW}Desarrollo General:${NC}"
	@echo "  ${GREEN}make setup${NC}                 - Configura el proyecto (instala dependencias)"
	@echo "  ${GREEN}make start${NC}                 - Inicia toda la aplicación (frontend + backend + DB)"
	@echo "  ${GREEN}make stop${NC}                  - Detiene todos los contenedores y servicios"
	@echo "  ${GREEN}make clean${NC}                 - Limpia archivos generados y cachés"
	@echo ""
	@echo "${YELLOW}Backend (Spring Boot):${NC}"
	@echo "  ${GREEN}make backend-build${NC}         - Construye el backend (sin tests)"
	@echo "  ${GREEN}make backend-test${NC}          - Ejecuta tests del backend"
	@echo "  ${GREEN}make backend-run${NC}           - Ejecuta el backend"
	@echo "  ${GREEN}make backend-debug${NC}         - Ejecuta el backend en modo debug"
	@echo "  ${GREEN}make backend-clean${NC}         - Limpia el directorio target del backend"
	@echo ""
	@echo "${YELLOW}Frontend (Angular):${NC}"
	@echo "  ${GREEN}make frontend-install${NC}      - Instala dependencias del frontend"
	@echo "  ${GREEN}make frontend-build${NC}        - Construye el frontend para producción"
	@echo "  ${GREEN}make frontend-run${NC}          - Ejecuta el servidor de desarrollo frontend"
	@echo "  ${GREEN}make frontend-test${NC}         - Ejecuta tests del frontend"
	@echo "  ${GREEN}make frontend-lint${NC}         - Ejecuta el linter en el código frontend"
	@echo ""
	@echo "${YELLOW}Docker:${NC}"
	@echo "  ${GREEN}make docker-up${NC}             - Inicia contenedores de Docker"
	@echo "  ${GREEN}make docker-down${NC}           - Detiene contenedores de Docker"
	@echo "  ${GREEN}make docker-clean${NC}          - Limpia volúmenes y contenedores (¡borra datos!)"
	@echo ""
	@echo "${YELLOW}Documentación:${NC}"
	@echo "  ${GREEN}make update-docs${NC}           - Actualiza la documentación de estructura del proyecto"
	@echo "  ${GREEN}make view-docs${NC}             - Abre el directorio de documentación"
	@echo ""
	@echo "${YELLOW}Utilidades:${NC}"
	@echo "  ${GREEN}make kill-port PORT=xxxx${NC}   - Mata el proceso que usa el puerto especificado"
	@echo ""

# Tareas generales
.PHONY: setup start stop clean

setup: frontend-install backend-build docker-up
	@echo "${GREEN}✅ Entorno configurado correctamente${NC}"

start: docker-up backend-run frontend-run
	@echo "${GREEN}✅ Aplicación iniciada${NC}"

stop:
	@if lsof -i :$(PORT_FRONTEND) >/dev/null 2>&1; then \
	  echo "🛑 Parando frontend..."; \
	  npx kill-port $(PORT_FRONTEND) -y; \
	  echo "✅ Frontend detenido"; \
	else \
	  echo "✅ Frontend no estaba en ejecución"; \
	fi

	@cd $(BACKEND_DIR) && mvn spring-boot:stop
	@sleep 2  # Esperar un poco para que se detenga correctamente

	@if lsof -i :$(PORT_BACKEND) >/dev/null 2>&1; then \
	  echo "🛑 Parando backend..."; \
	  npx kill-port $(PORT_BACKEND) -y; \
	  echo "✅ Backend detenido"; \
	else \
	  echo "✅ Backend no estaba en ejecución"; \
	fi

	@make docker-down
	@echo "${GREEN}✅ Todos los servicios han sido detenidos${NC}"


clean: backend-clean frontend-clean docker-clean
	@echo "${GREEN}✅ Limpieza completa realizada${NC}"

# Tareas Frontend
.PHONY: frontend-install frontend-build frontend-run frontend-test frontend-lint frontend-clean

frontend-install:
	@echo "${BLUE}📦 Instalando dependencias del frontend...${NC}"
	@cd $(FRONTEND_DIR) && npm install

frontend-build:
	@echo "${BLUE}🔨 Construyendo el frontend...${NC}"
	@cd $(FRONTEND_DIR) && npm run build

frontend-run:
	@echo "${BLUE}🚀 Iniciando servidor de desarrollo frontend...${NC}"
	@sleep 2 #espera a que el backend se inicie
	@cd $(FRONTEND_DIR) && bun run start
	

frontend-test:
	@echo "${BLUE}🧪 Ejecutando tests del frontend...${NC}"
	@cd $(FRONTEND_DIR) && npm run test

frontend-lint:
	@echo "${BLUE}🔍 Ejecutando linter en el frontend...${NC}"
	@cd $(FRONTEND_DIR) && npm run lint

frontend-clean:
	@echo "${BLUE}🧹 Limpiando directorio dist del frontend...${NC}"
	@rm -rf $(FRONTEND_DIR)/dist
	@rm -rf $(FRONTEND_DIR)/.angular/cache

# Tareas Backend
.PHONY: backend-build backend-test backend-run backend-debug backend-clean

backend-build:
	@echo "${BLUE}🔨 Construyendo el backend (sin tests)...${NC}"
	@cd $(BACKEND_DIR) && mvn clean install -DskipTests

backend-test:
	@echo "${BLUE}🧪 Ejecutando tests del backend...${NC}"
	@cd $(BACKEND_DIR) && mvn clean test

backend-run:
	@echo "${BLUE}🚀 Iniciando servidor backend...${NC}"
	@cd $(BACKEND_DIR) && mvn spring-boot:run &

backend-debug:
	@echo "${BLUE}🐛 Iniciando servidor backend en modo debug...${NC}"
	@cd $(BACKEND_DIR) && mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005" &

backend-clean:
	@echo "${BLUE}🧹 Limpiando directorio target del backend...${NC}"
	@cd $(BACKEND_DIR) && mvn clean

# Tareas Docker
.PHONY: docker-up docker-down docker-clean

docker-up:
	@echo "${BLUE}🐳 Iniciando contenedores Docker...${NC}"
	@docker compose up -d && sleep 5

docker-down:
	@echo "${BLUE}🐳 Deteniendo contenedores Docker...${NC}"
	@docker compose down

docker-clean:
	@echo "${RED}⚠️  ATENCIÓN: Se eliminarán todos los volúmenes y datos!${NC}"
	@echo "${YELLOW}¿Deseas continuar? (s/N)${NC}"
	@read -p "" confirm; \
	if [ "$$confirm" = "s" ] || [ "$$confirm" = "S" ]; then \
		echo "${BLUE}🧹 Limpiando contenedores y volúmenes...${NC}"; \
		docker compose down --volumes --remove-orphans; \
	else \
		echo "${YELLOW}Operación cancelada${NC}"; \
	fi

# Tareas Documentación
.PHONY: update-docs view-docs

update-docs:
	@echo "${BLUE}📚 Actualizando documentación de estructura...${NC}"
	@make tree > $(DOCS_DIR)/dev-strategy/tree.md
	@echo "${GREEN}✅ Documentación actualizada${NC}"
# Genera un árbol de la estructura del proyecto, excluyendo directorios específicos
tree:
	@echo "${BLUE}📂 Generando árbol de la estructura del proyecto...${NC}"
	@tree -I "node_modules|.git|.angular|.idea|dist|.vscode|target|build" -L 15 $(PROJECT_DIR)
	
view-docs:
	@echo "${BLUE}🔍 Abriendo directorio de documentación...${NC}"
	@xdg-open $(DOCS_DIR) 2>/dev/null || open $(DOCS_DIR) 2>/dev/null || explorer $(DOCS_DIR) 2>/dev/null || echo "${YELLOW}No se pudo abrir el directorio de documentación${NC}"

# Utilidades
.PHONY: kill-port

kill-port:
	@if [ -z "$(PORT)" ]; then \
		echo "${RED}❌ Debes especificar un puerto: make kill-port PORT=xxxx${NC}"; \
		exit 1; \
	fi
	@echo "${BLUE}🔪 Matando proceso en puerto $(PORT)...${NC}"
	@lsof -ti:$(PORT) | xargs kill -9 2>/dev/null || echo "${YELLOW}No hay procesos en el puerto $(PORT)${NC}"

# Utilidades para IA y desarrollo
.PHONY: ai-help dev-logs check-structure generate-component

ai-help:
	@echo "${BLUE}🤖 Comandos Útiles para Agentes IA${NC}"
	@echo ""
	@echo "${YELLOW}Rutas importantes:${NC}"
	@echo "  Frontend: $(FRONTEND_DIR)"
	@echo "  Backend: $(BACKEND_DIR)"
	@echo "  Documentación: $(DOCS_DIR)"
	@echo ""
	@echo "${YELLOW}Estructura:${NC}"
	@echo "  make check-structure       - Muestra estructura básica del proyecto"
	@echo ""
	@echo "${YELLOW}Generación de componentes:${NC}"
	@echo "  make generate-component COMP_NAME=nombre COMP_TYPE=tipo"
	@echo "    - COMP_NAME: Nombre del componente"
	@echo "    - COMP_TYPE: Tipo (component|service|directive|pipe)"
	@echo ""

# Muestra la estructura de directorios del frontend y backend, útil para entender la organización
check-structure:
	@echo "${BLUE}📁 Estructura básica del proyecto${NC}"
	@echo "${YELLOW}Estructura del frontend:${NC}"
	@find $(FRONTEND_DIR)/src/app -type d -not -path "*/node_modules/*" -not -path "*/\.*" | sort
	@echo "${YELLOW}Estructura del backend:${NC}"
	@find $(BACKEND_DIR)/src -type d -not -path "*/\.*" | sort

# Genera un nuevo componente Angular con la estructura correcta siguiendo las mejores prácticas
# Uso: make generate-component COMP_NAME=nombre COMP_TYPE=tipo
generate-component:
	@if [ -z "$(COMP_NAME)" ]; then \
		echo "${RED}❌ Debes especificar un nombre: make generate-component COMP_NAME=nombre COMP_TYPE=tipo${NC}"; \
		exit 1; \
	fi
	@COMP_TYPE_VALUE=$${COMP_TYPE:-component}; \
	echo "${BLUE}🔨 Generando $$COMP_TYPE_VALUE: $(COMP_NAME)...${NC}"; \
	cd $(FRONTEND_DIR) && ng generate $$COMP_TYPE_VALUE $(COMP_NAME) --standalone

# Muestra los últimos logs de desarrollo tanto del frontend como del backend
dev-logs:
	@echo "${BLUE}📋 Mostrando logs de desarrollo...${NC}"
	@echo "${YELLOW}Logs del frontend:${NC}"
	@tail -n 20 $(FRONTEND_DIR)/npm-debug.log 2>/dev/null || echo "No hay logs del frontend"
	@echo "${YELLOW}Logs del backend:${NC}"
	@tail -n 20 $(BACKEND_DIR)/logs/spring.log 2>/dev/null || echo "No hay logs del backend"

# Comandos para poner el agente de IA en contexto con información del proyecto
.PHONY: ai-context

# Proporciona contexto del proyecto para agentes de IA, guiándolos a la documentación principal
ai-context:
	@echo "${BLUE}🤖 Contexto del Proyecto BidMaster para Agentes IA${NC}"
	@echo ""
	@echo "Hemos trabajado juntos en este proyecto y logrado grandes avances, pero quiero asegurarnos de que comprendas el contexto nuevamente para mantener la agilidad y evitar dificultades en el desarrollo."
	@echo ""
	@echo "Para ello, revisa detenidamente el documento docs/dev-strategy/ai-strategy.md. Este documento define el flujo y la estructura del proyecto, por lo que es crucial que lo analices en detalle y sigas sus recomendaciones y procesos."
	@echo ""
	@echo "Además, el documento puede referenciar otras documentaciones útiles. Léelas todas, ya que te ayudarán a comprender mejor el ecosistema del proyecto y a integrarte de manera eficiente en el desarrollo."
	@echo ""
	@echo "Ejecuta el comando make tree para ver la estructura del proyecto."
	@echo ""
	@echo "Si tienes alguna pregunta o necesitas ayuda, no dudes en preguntar. Estoy aquí para ayudarte."
	@echo ""
	@echo "¡Buena suerte!"