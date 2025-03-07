# BidMaster - Fullstack Demo Project

Modern fullstack application demonstrating the integration of:
- Frontend: Angular 19 (Standalone, Signals, Zoneless) with Tailwind CSS and Bun
- Backend: Spring Boot 3 with Java 17 (Hexagonal Architecture)
- Database: PostgreSQL 16
- Message Broker: Apache Kafka
- Container Orchestration: Docker Compose

## Project Structure
```
bidmaster-fullstack/
├── bidmaster-front/    # Angular 19 application with Tailwind CSS
├── bidmaster-ws/       # Spring Boot application with Hexagonal Architecture
├── docs/              # Project documentation and development strategy
├── Makefile           # Centralized build and management commands
└── docker-compose.yml # Main docker-compose configuration
```

## Prerequisites
- Docker and Docker Compose
- Bun 1.0+ or Node.js 18+
- Java 17
- Maven 3.8+
- GNU Make (for Makefile support)

## Quick Start with Makefile

The project includes a comprehensive Makefile that simplifies development tasks:

```bash
# Setup and start everything
make setup
make start

# View all available commands
make help

# View AI-specific helpful commands
make ai-help
```

## Getting Started (Manual Method)
1. Clone the repository
2. Run `docker-compose up` to start database and kafka services
3. Start the backend with `cd bidmaster-ws && mvn spring-boot:run`
4. Start the frontend with `cd bidmaster-front && bun run start` or `ng serve`
5. Frontend will be available at http://localhost:4200
6. Backend API will be available at http://localhost:8080

## Services
- Frontend (Angular): http://localhost:4200
- Backend (Spring Boot): http://localhost:8080
- PostgreSQL: localhost:5432
- Kafka: localhost:9092
- Kafka UI: http://localhost:8085

## Development

### Using Makefile (Recommended)
```bash
# Setup and start everything
make setup
make start

# Stop everything
make stop

# Start frontend development server
make frontend-run

# Start backend in debug mode
make backend-debug

# Update project structure documentation
make update-docs

# Show project structure
make tree
```

### Manual Commands

#### Frontend
```bash
cd bidmaster-front
bun install  # or npm install
bun run start  # or npm start
```

#### Backend
```bash
cd bidmaster-ws
./mvnw spring-boot:run
```

## Documentation

The project includes comprehensive documentation in the `/docs` directory:

- `/docs/dev-strategy/` - Development guidelines and best practices
  - `ai-strategy.md` - Central reference document for AI-assisted development
  - `angular_best_practices.md` - Angular development standards
  - `springboot_best_practices.md` - Spring Boot development standards
  
- `/docs/architecture/` - Detailed architecture and business logic documentation
  - Domain model descriptions
  - Entity relationships
  - Business rules
  - State transition diagrams

## Key Technical Features

### Frontend
- Modern Angular features (Standalone Components, Signals)
- Dynamic and reusable component architecture
- Responsive UI with Tailwind CSS
- Dynamic sidebar and dashboard implementation using maintainable array structures
- Reusable form components with consistent validation

### Backend
- Hexagonal architecture (ports and adapters)
- Reactive Spring Boot backend
- Real-time updates via Kafka
- Comprehensive domain model implementation

### Infrastructure
- Containerized development environment
- Kafka UI for message monitoring
- Integrated development workflow via Makefile
