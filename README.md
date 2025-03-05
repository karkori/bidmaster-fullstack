# Fullstack Demo Project

Modern fullstack application demonstrating the integration of:
- Frontend: Angular 19 (Standalone, Signals, Zoneless)
- Backend: Spring Boot
- Database: PostgreSQL
- Message Broker: Apache Kafka
- Container Orchestration: Docker Compose

## Project Structure
```
fullstack-demo/
├── frontend/           # Angular 19 application
├── backend/            # Spring Boot application
├── docker/            # Docker configuration files
└── docker-compose.yml # Main docker-compose configuration
```

## Prerequisites
- Docker and Docker Compose
- Node.js 20+
- Java 21
- Maven

## Getting Started
1. Clone the repository
2. Run `docker-compose up` to start all services
3. Frontend will be available at http://localhost:4200
4. Backend API will be available at http://localhost:8080

## Services
- Frontend (Angular): http://localhost:4200
- Backend (Spring Boot): http://localhost:8080
- PostgreSQL: localhost:5432
- Kafka: localhost:9092
- Kafka UI: http://localhost:8085
