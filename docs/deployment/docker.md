# 🐳 Configuración Docker

Este documento detalla la configuración de contenedores Docker para el despliegue de BidMaster.

## 📋 Contenedores Principales

### Backend - Spring Boot (`bidmaster-ws`)
```yaml
bidmaster-ws:
  build: ./bidmaster-ws
  ports:
    - "8080:8080"
  environment:
    - SPRING_PROFILES_ACTIVE=dev
    - SPRING_DATASOURCE_URL=jdbc:postgresql://bidmaster-db:5432/bidmaster
    - SPRING_DATASOURCE_USERNAME=postgres
    - SPRING_DATASOURCE_PASSWORD=postgres
    - MINIO_ENDPOINT=http://minio:9000
    - MINIO_ACCESS_KEY=minioadmin
    - MINIO_SECRET_KEY=minioadmin
    - CLAMAV_HOST=clamav
    - CLAMAV_PORT=3310
  depends_on:
    - bidmaster-db
    - minio
    - clamav
```

### Frontend - Angular (`bidmaster-front`)
```yaml
bidmaster-front:
  build: ./bidmaster-front
  ports:
    - "4200:4200"
  volumes:
    - ./bidmaster-front:/app
    - /app/node_modules
```

### Base de Datos - PostgreSQL (`bidmaster-db`)
```yaml
bidmaster-db:
  image: postgres:14
  ports:
    - "5432:5432"
  environment:
    - POSTGRES_DB=bidmaster
    - POSTGRES_USER=postgres
    - POSTGRES_PASSWORD=postgres
  volumes:
    - postgres-data:/var/lib/postgresql/data
```

## 🖼️ Contenedores para Gestión de Imágenes

### MinIO - Almacenamiento S3 Compatible
```yaml
minio:
  image: minio/minio:RELEASE.2023-01-20T02-05-44Z
  container_name: bidmaster-minio
  ports:
    - "9000:9000"
    - "9001:9001"
  environment:
    MINIO_ROOT_USER: minioadmin
    MINIO_ROOT_PASSWORD: minioadmin
  volumes:
    - minio-data:/data
  command: server /data --console-address ":9001"
  healthcheck:
    test: ["CMD", "curl", "-f", "http://localhost:9000/minio/health/live"]
    interval: 30s
    timeout: 20s
    retries: 3
```

### ClamAV - Escaneo Antivirus
```yaml
clamav:
  image: clamav/clamav:latest
  container_name: bidmaster-clamav
  ports:
    - "3310:3310"
  volumes:
    - clamav-data:/var/lib/clamav
  environment:
    - CLAMAV_NO_MILTERD=true
  restart: unless-stopped
```

## 🔄 Volúmenes Persistentes
```yaml
volumes:
  postgres-data:
  minio-data:
  clamav-data:
```

## 🔧 Configuración de Redes
Las redes entre contenedores se establecen automáticamente a través de Docker Compose, permitiendo la comunicación entre servicios mediante sus nombres de servicio como host.

## 🚀 Comandos de Despliegue

### Iniciar todos los servicios
```bash
docker-compose up -d
```

### Detener todos los servicios
```bash
docker-compose down
```

### Reconstruir e iniciar servicios específicos
```bash
docker-compose up -d --build bidmaster-ws bidmaster-front
```

### Ver logs de servicios específicos
```bash
docker-compose logs -f bidmaster-ws minio clamav
```

## 🔍 Verificación del Despliegue

### MinIO
- Acceso a la consola: http://localhost:9001
- Credenciales: minioadmin / minioadmin

### ClamAV
Para verificar que ClamAV está funcionando correctamente:
```bash
docker exec bidmaster-clamav clamdscan --version
```

### Prueba de integración
Para verificar la conexión desde el servicio principal al almacenamiento:
```bash
docker exec bidmaster-ws curl -I http://minio:9000
```
