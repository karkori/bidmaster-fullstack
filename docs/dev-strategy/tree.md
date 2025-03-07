/home/mustafa/CascadeProjects/fullstack-demo
├── bidmaster-front
│   ├── angular.json
│   ├── bun.lock
│   ├── Dockerfile
│   ├── package.json
│   ├── package-lock.json
│   ├── public
│   │   ├── favicon.ico
│   │   └── logo.svg
│   ├── README.md
│   ├── src
│   │   ├── app
│   │   │   ├── app.component.spec.ts
│   │   │   ├── app.component.ts
│   │   │   ├── app.config.server.ts
│   │   │   ├── app.config.ts
│   │   │   ├── app.routes.ts
│   │   │   ├── components
│   │   │   ├── core
│   │   │   │   ├── guards
│   │   │   │   │   └── auth.guard.ts
│   │   │   │   ├── interceptors
│   │   │   │   │   └── auth.interceptor.ts
│   │   │   │   └── services
│   │   │   │       └── auth.service.ts
│   │   │   ├── layouts
│   │   │   │   ├── dashboard-layout
│   │   │   │   │   ├── dashboard-layout.component.html
│   │   │   │   │   └── dashboard-layout.component.ts
│   │   │   │   ├── header
│   │   │   │   │   └── header.component.ts
│   │   │   │   └── main-layout
│   │   │   │       └── main-layout.component.ts
│   │   │   ├── pages
│   │   │   │   ├── auth
│   │   │   │   │   ├── login
│   │   │   │   │   │   └── login.component.ts
│   │   │   │   │   └── register
│   │   │   │   │       └── register.component.ts
│   │   │   │   ├── dashboard
│   │   │   │   │   ├── admin
│   │   │   │   │   │   ├── auctions
│   │   │   │   │   │   └── users
│   │   │   │   │   │       └── users.component.ts
│   │   │   │   │   ├── create-auction
│   │   │   │   │   │   ├── create-auction.component.html
│   │   │   │   │   │   └── create-auction.component.ts
│   │   │   │   │   ├── favorites
│   │   │   │   │   │   └── favorites.component.ts
│   │   │   │   │   ├── messages
│   │   │   │   │   │   ├── messages.component.html
│   │   │   │   │   │   └── messages.component.ts
│   │   │   │   │   ├── my-bids
│   │   │   │   │   │   └── my-bids.component.ts
│   │   │   │   │   ├── notifications
│   │   │   │   │   │   ├── notifications.component.html
│   │   │   │   │   │   └── notifications.component.ts
│   │   │   │   │   └── profile
│   │   │   │   │       └── profile.component.ts
│   │   │   │   ├── home
│   │   │   │   │   └── home.component.ts
│   │   │   │   ├── my-bids
│   │   │   │   │   ├── my-bids.component.html
│   │   │   │   │   └── my-bids.component.ts
│   │   │   │   └── profile
│   │   │   │       ├── profile.component.html
│   │   │   │       └── profile.component.ts
│   │   │   └── shared
│   │   │       ├── components
│   │   │       │   ├── auth-button
│   │   │       │   │   └── auth-button.component.ts
│   │   │       │   ├── category-nav
│   │   │       │   │   └── category-nav.component.ts
│   │   │       │   └── search-bar
│   │   │       │       └── search-bar.component.ts
│   │   │       ├── models
│   │   │       │   └── user.model.ts
│   │   │       └── validators
│   │   │           └── auth.schemas.ts
│   │   ├── environments
│   │   │   └── environment.ts
│   │   ├── index.html
│   │   ├── main.server.ts
│   │   ├── main.ts
│   │   ├── server.ts
│   │   └── styles.css
│   ├── tsconfig.app.json
│   ├── tsconfig.json
│   └── tsconfig.spec.json
├── bidmaster-ws
│   ├── Dockerfile
│   ├── HELP.md
│   ├── mvnw
│   ├── mvnw.cmd
│   ├── pom.xml
│   └── src
│       ├── main
│       │   ├── java
│       │   │   └── dev
│       │   │       └── mostapha
│       │   │           └── bidmaster
│       │   │               ├── adapter
│       │   │               │   ├── in
│       │   │               │   │   └── web
│       │   │               │   │       ├── controller
│       │   │               │   │       │   └── UserController.java
│       │   │               │   │       ├── dto
│       │   │               │   │       │   ├── AddressDTO.java
│       │   │               │   │       │   ├── auth
│       │   │               │   │       │   ├── BalanceOperationDTO.java
│       │   │               │   │       │   ├── request
│       │   │               │   │       │   │   ├── LoginRequestDTO.java
│       │   │               │   │       │   │   └── UserRequestDTO.java
│       │   │               │   │       │   ├── response
│       │   │               │   │       │   │   └── LoginResponseDTO.java
│       │   │               │   │       │   └── UserResponseDTO.java
│       │   │               │   │       ├── exception
│       │   │               │   │       └── mapper
│       │   │               │   │           └── UserMapper.java
│       │   │               │   └── out
│       │   │               │       └── persistence
│       │   │               │           └── UserRepositoryAdapter.java
│       │   │               ├── application
│       │   │               │   ├── port
│       │   │               │   │   ├── in
│       │   │               │   │   │   └── UserUseCase.java
│       │   │               │   │   └── out
│       │   │               │   │       └── UserRepository.java
│       │   │               │   └── service
│       │   │               │       └── UserService.java
│       │   │               ├── BidMasterApplication.java
│       │   │               ├── config
│       │   │               │   ├── FlywayDatabaseInitializerConfig.java
│       │   │               │   └── R2dbcConfig.java
│       │   │               ├── domain
│       │   │               │   ├── model
│       │   │               │   │   └── user
│       │   │               │   │       ├── Address.java
│       │   │               │   │       ├── User.java
│       │   │               │   │       ├── UserRole.java
│       │   │               │   │       └── UserStatus.java
│       │   │               │   └── service
│       │   │               └── infrastructure
│       │   │                   ├── config
│       │   │                   │   └── WebConfig.java
│       │   │                   └── persistence
│       │   │                       ├── entities
│       │   │                       │   └── UserEntity.java
│       │   │                       └── repositories
│       │   │                           └── InfraUserRepository.java
│       │   └── resources
│       │       ├── application.properties_old
│       │       ├── application.yaml
│       │       ├── application.yaml_old
│       │       ├── application.yaml_postgresql
│       │       └── db
│       │           └── migration
│       │               └── V1__init_schema.sql
│       └── test
│           ├── java
│           │   └── dev
│           │       └── mostapha
│           │           └── bidmaster
│           │               └── BidMasterApplicationTests.java
│           └── resources
│               └── application-test.properties
├── comands.txt
├── docker-compose.yml
├── docker-compose.yml_old
├── docs
│   ├── api
│   ├── architecture
│   │   ├── domain
│   │   │   ├── entities
│   │   │   │   ├── address.md
│   │   │   │   ├── auction.md
│   │   │   │   ├── audit-log.md
│   │   │   │   ├── bid.md
│   │   │   │   ├── category.md
│   │   │   │   ├── comment.md
│   │   │   │   ├── notification.md
│   │   │   │   ├── payment.md
│   │   │   │   ├── permission.md
│   │   │   │   ├── product.md
│   │   │   │   ├── rating.md
│   │   │   │   ├── README.md
│   │   │   │   ├── report.md
│   │   │   │   ├── shipping.md
│   │   │   │   ├── system-config.md
│   │   │   │   ├── tag.md
│   │   │   │   └── user.md
│   │   │   ├── README.md
│   │   │   ├── rules
│   │   │   │   ├── admin-rules.md
│   │   │   │   ├── auction-rules.md
│   │   │   │   ├── notification-rules.md
│   │   │   │   ├── payment-rules.md
│   │   │   │   ├── product-rules.md
│   │   │   │   ├── rating-comment-rules.md
│   │   │   │   ├── README.md
│   │   │   │   ├── security-rules.md
│   │   │   │   ├── shipping-rules.md
│   │   │   │   └── user-rules.md
│   │   │   └── states
│   │   │       ├── auction-states.md
│   │   │       ├── bid-states.md
│   │   │       ├── payment-states.md
│   │   │       ├── product-states.md
│   │   │       ├── rating-comment-states.md
│   │   │       ├── README.md
│   │   │       ├── report-states.md
│   │   │       ├── shipping-states.md
│   │   │       ├── state-logging.md
│   │   │       ├── system-events.md
│   │   │       └── user-states.md
│   │   └── domain-model.md
│   ├── database
│   ├── deployment
│   ├── dev-strategy
│   │   ├── ai-strategy.md
│   │   ├── angular_best_practices.md
│   │   ├── springboot_best_practices.md
│   │   ├── tree.md
│   │   └── tree_raw.md
│   ├── README.md
│   ├── use-cases
│   │   ├── admin
│   │   │   └── README.md
│   │   ├── system
│   │   │   └── README.md
│   │   └── user
│   │       └── README.md
│   └── use-cases.md
└── README.md

96 directories, 139 files
