# BidMaster - Fullstack Demo Project

Modern fullstack application demonstrating the integration of:
- Frontend: Angular 19 (Standalone, Signals, Zoneless) with Tailwind CSS and Bun
- Backend: Spring Boot 3 with Java 17
- Database: PostgreSQL 16
- Message Broker: Apache Kafka
- Container Orchestration: Docker Compose

## Project Structure
```
fullstack-demo/
├── bidmaster-front/    # Angular 19 application with Tailwind CSS
├── bidmaster-ws/       # Spring Boot application
└── docker-compose.yml # Main docker-compose configuration
```

## Prerequisites
- Docker and Docker Compose
- Bun 1.0+
- Java 17
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

## Development

### Frontend
```bash
cd bidmaster-front
bun install
bun run start
```

### Backend
```bash
cd bidmaster-ws
./mvnw spring-boot:run
```

## Features
- Modern Angular features (Standalone Components, Signals)
- Reactive Spring Boot backend
- Real-time updates via Kafka
- Responsive UI with Tailwind CSS
- Containerized development environment
- Kafka UI for message monitoring
