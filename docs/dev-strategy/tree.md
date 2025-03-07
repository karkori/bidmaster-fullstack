# 🌳 Estructura de Directorios del Proyecto

Este documento mantiene una representación actualizada de la estructura de directorios del proyecto, excluyendo directorios como `node_modules`, `.git`, `.angular`, etc. para mayor claridad.

## 📁 Estructura Completa

```
/home/mustafa/CascadeProjects/fullstack-demo
├── bidmaster-front
│   ├── angular.json
│   ├── bun.lock
│   ├── Dockerfile
│   ├── package.json
│   ├── package-lock.json
│   ├── public
│   │   ├── favicon.ico
│   │   └── logo.svg
│   ├── README.md
│   ├── src
│   │   ├── app
│   │   │   ├── app.component.spec.ts
│   │   │   ├── app.component.ts
│   │   │   ├── app.config.server.ts
│   │   │   ├── app.config.ts
│   │   │   ├── app.routes.ts
│   │   │   ├── components
│   │   │   ├── core
│   │   │   │   ├── guards
│   │   │   │   │   └── auth.guard.ts
│   │   │   │   ├── interceptors
│   │   │   │   │   └── auth.interceptor.ts
│   │   │   │   └── services
│   │   │   │       └── auth.service.ts
│   │   │   ├── layouts
│   │   │   │   ├── header
│   │   │   │   │   └── header.component.ts
│   │   │   │   └── main-layout
│   │   │   │       └── main-layout.component.ts
│   │   │   ├── pages
│   │   │   │   ├── auth
│   │   │   │   │   ├── login
│   │   │   │   │   │   └── login.component.ts
│   │   │   │   │   └── register
│   │   │   │   │       └── register.component.ts
│   │   │   │   ├── home
│   │   │   │   │   └── home.component.ts
│   │   │   │   ├── my-bids
│   │   │   │   │   ├── my-bids.component.html
│   │   │   │   │   └── my-bids.component.ts
│   │   │   │   └── profile
│   │   │   │       ├── profile.component.html
│   │   │   │       └── profile.component.ts
│   │   │   └── shared
│   │   │       ├── components
│   │   │       │   ├── auth-button
│   │   │       │   │   └── auth-button.component.ts
│   │   │       │   ├── category-nav
│   │   │       │   │   └── category-nav.component.ts
│   │   │       │   └── search-bar
│   │   │       │       └── search-bar.component.ts
│   │   │       ├── models
│   │   │       │   └── user.model.ts
│   │   │       └── validators
│   │   │           └── auth.schemas.ts
│   │   ├── environments
│   │   │   └── environment.ts
│   │   ├── index.html
│   │   ├── main.server.ts
│   │   ├── main.ts
│   │   ├── server.ts
│   │   └── styles.css
│   ├── tsconfig.app.json
│   ├── tsconfig.json
│   └── tsconfig.spec.json
├── bidmaster-ws
│   ├── Dockerfile
│   ├── HELP.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── dev
│       │   │       └── mostapha
│       │   │           └── bidmaster
│       │   │               ├── adapter
│       │   │               │   ├── in
│       │   │               │   │   └── web
│       │   │               │   │       ├── controller
│       │   │               │   │       │   └── UserController.java
│       │   │               │   │       ├── dto
│       │   │               │   │       │   ├── AddressDTO.java
│       │   │               │   │       │   ├── BalanceOperationDTO.java
│       │   │               │   │       │   ├── request
│       │   │               │   │       │   │   ├── LoginRequestDTO.java
│       │   │               │   │       │   │   └── UserRequestDTO.java
│       │   │               │   │       │   ├── response
│       │   │               │   │       │   │   └── LoginResponseDTO.java
│       │   │               │   │       │   └── UserResponseDTO.java
│       │   │               │   │       ├── exception
│       │   │               │   │       └── mapper
│       │   │               │   │           └── UserMapper.java
│       │   │               │   └── out
│       │   │               │       └── persistence
│       │   │               ├── application
│       │   │               │   ├── port
│       │   │               │   │   ├── in
│       │   │               │   │   │   └── UserUseCase.java
│       │   │               │   │   └── out
│       │   │               │   │       └── UserOutPort.java
│       │   │               │   └── service
│       │   │               │       └── UserService.java
│       │   │               ├── config
│       │   │               │   ├── FlywayDatabaseInitializerConfig.java
│       │   │               │   └── R2dbcConfig.java
│       │   │               ├── domain
│       │   │               │   └── model
│       │   │               │       ├── auction
│       │   │               │       ├── bid
│       │   │               │       ├── product
│       │   │               │       └── user
│       │   │               │           ├── Address.java
│       │   │               │           └── User.java
│       │   │               └── infrastructure
│       │   │                   └── persistence
│       │   │                       ├── entities
│       │   │                       │   ├── AddressEntity.java
│       │   │                       │   └── UserEntity.java
│       │   │                       └── repositories
│       │   │                           └── UserRepository.java
│       │   └── resources
│       │       ├── application.yaml
│       │       └── db
│       │           └── migration
│       │               └── V1__init.sql
│       └── test
│           ├── java
│           │   └── dev
│           └── resources
├── docker-compose.yml
├── docs
│   ├── api
│   ├── architecture
│   │   ├── domain
│   │   │   ├── entities
│   │   │   ├── README.md
│   │   │   ├── rules
│   │   │   └── states
│   │   └── domain-model.md
│   ├── database
│   ├── deployment
│   ├── dev-strategy
│   │   ├── ai-strategy.md
│   │   ├── angular_best_practices.md
│   │   ├── tree.md
│   │   └── tree_raw.md
│   ├── README.md
│   ├── use-cases
│   └── use-cases.md
└── README.md
```

## 📱 Frontend (Angular) - Estructura Principal

```
bidmaster-front/
├── src/
│   ├── app/
│   │   ├── components/           # @components/*
│   │   ├── core/                 # @core/*
│   │   │   ├── guards/
│   │   │   │   └── auth.guard.ts
│   │   │   ├── interceptors/
│   │   │   │   └── auth.interceptor.ts
│   │   │   └── services/
│   │   │       └── auth.service.ts
│   │   ├── layouts/              # @layouts/*
│   │   │   ├── header/
│   │   │   └── main-layout/
│   │   ├── pages/                # @pages/*
│   │   │   ├── auth/
│   │   │   │   ├── login/
│   │   │   │   └── register/
│   │   │   ├── home/
│   │   │   ├── my-bids/
│   │   │   └── profile/
│   │   └── shared/               # @shared/*
│   │       ├── components/
│   │       │   ├── auth-button/
│   │       │   ├── category-nav/
│   │       │   └── search-bar/
│   │       ├── models/
│   │       │   └── user.model.ts
│   │       └── validators/
│   │           └── auth.schemas.ts
│   ├── environments/         # @environments/*
│   │   └── environment.ts
│   ├── index.html
│   ├── main.ts
│   └── styles.css
└── tsconfig.json            # Contiene la configuración de aliases
```

## 🖥️ Backend (Spring WebFlux) - Estructura Principal

```
bidmaster-ws/
└── src/
    ├── main/
    │   ├── java/dev/mostapha/bidmaster/
    │   │   ├── adapter/
    │   │   │   ├── in/web/
    │   │   │   │   ├── controller/
    │   │   │   │   ├── dto/
    │   │   │   │   │   ├── request/
    │   │   │   │   │   │   ├── LoginRequestDTO.java
    │   │   │   │   │   │   └── UserRequestDTO.java
    │   │   │   │   │   └── response/
    │   │   │   │   │       └── LoginResponseDTO.java
    │   │   │   │   ├── exception/
    │   │   │   │   └── mapper/
    │   │   │   └── out/persistence/
    │   │   ├── application/
    │   │   │   ├── port/
    │   │   │   │   ├── in/
    │   │   │   │   │   └── UserUseCase.java
    │   │   │   │   └── out/
    │   │   │   └── service/
    │   │   │       └── UserService.java
    │   │   ├── config/
    │   │   │   ├── FlywayDatabaseInitializerConfig.java
    │   │   │   └── R2dbcConfig.java
    │   │   ├── domain/model/
    │   │   │   └── user/
    │   │   │       ├── Address.java
    │   │   │       └── User.java
    │   │   └── infrastructure/persistence/
    │   │       ├── entities/
    │   │       │   ├── AddressEntity.java
    │   │       │   └── UserEntity.java
    │   │       └── repositories/
    │   │           └── UserRepository.java
    │   └── resources/
    │       ├── application.yaml
    │       └── db/migration/
    │           └── V1__init.sql
    └── test/
```

---

---

> **Nota:** Este documento se actualizará cada vez que se realicen modificaciones importantes en la estructura del proyecto.