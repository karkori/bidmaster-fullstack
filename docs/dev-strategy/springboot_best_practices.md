# Guía de Mejores Prácticas para el Backend (Spring Boot 3)

Este documento establece las directrices y estándares para el desarrollo del backend de BidMaster usando Spring Boot 3 con arquitectura hexagonal.

## Arquitectura Hexagonal

La arquitectura hexagonal (también conocida como Ports and Adapters) nos permite separar claramente la lógica de negocio de los detalles de implementación, haciendo el sistema más mantenible, testeable y adaptable a cambios.

### Estructura de Paquetes

Nuestro proyecto sigue una estructura de paquetes basada en arquitectura hexagonal:

```
src/main/java/dev/mostapha/bidmaster/
├── adapter/           # Adaptadores (implementaciones concretas)
│   ├── in/           # Adaptadores de entrada (controladores, APIs)
│   │   └── web/      # Controladores REST
│   └── out/          # Adaptadores de salida (persistencia, servicios externos)
│       └── persistence/ # Implementaciones de repositorios
├── application/      # Capa de aplicación
│   ├── port/         # Puertos (interfaces)
│   │   ├── in/       # Puertos de entrada (casos de uso)
│   │   └── out/      # Puertos de salida (repositorios)
│   └── service/      # Implementaciones de casos de uso
└── domain/           # Entidades y lógica de dominio
    └── model/        # Modelos de dominio
```
- Mejorar la experiencia de desarrollo con herramientas específicas para cada lenguaje

```typescript
// RECOMENDADO
@Component({
  selector: 'app-my-component',
  templateUrl: './my-component.component.html',
  styleUrls: ['./my-component.component.css']
})
export default class MyComponent { }
```

En lugar de:

```typescript
// EVITAR para componentes complejos
@Component({
  selector: 'app-my-component',
  template: `<div>Plantilla inline que dificulta la lectura cuando crece</div>`,
  styles: [`.my-style { color: red; }`]
})
export default class MyComponent { }
```

### Componentes Standalone
En Angular 19, los componentes son standalone por defecto. Esto significa que:
- No es necesario especificar `standalone: true` en el decorador `@Component`
- Los componentes pueden importarse directamente en otros componentes sin necesidad de un módulo

```typescript
@Component({
  selector: 'app-my-component',
  // No es necesario especificar standalone: true
  imports: [CommonModule, ReactiveFormsModule],
  template: `...`
})
export default class MyComponent { }
```

### Exportación por Defecto para Componentes de Página
Los componentes utilizados como páginas en las rutas deben exportarse como `default` para simplificar el enrutamiento:

```typescript
// CORRECTO
export default class LoginComponent { }

// EN EL ROUTER
{
  path: 'login',
  loadComponent: () => import('./pages/auth/login/login.component')
}
```

En lugar de:

```typescript
// INCORRECTO para componentes de página
export class LoginComponent { }

// EN EL ROUTER (requiere then)
{
  path: 'login',
  loadComponent: () => import('./pages/auth/login/login.component').then(m => m.LoginComponent)
}
```

## Inyección de Dependencias

### Uso de la función inject()
Preferir el uso de la función `inject()` en lugar de la inyección por constructor para:
- Tener un código más limpio y conciso
- Reducir la verbosidad de las clases
- Mejorar la legibilidad separando las inyecciones de la lógica de negocio

```typescript
// RECOMENDADO: Uso de inject()
export class MyComponent {
  private authService = inject(AuthService);
  private router = inject(Router);
  
  // Lógica del componente sin constructor
}
```

En lugar de:

```typescript
// EVITAR: Inyección por constructor
export class MyComponent {
  constructor(
    private authService: AuthService,
    private router: Router
  ) {}
  
  // Lógica del componente
}
```

## Gestión de Estado y Reactividad

### Uso de Signals
Usar signals en lugar de BehaviorSubject/Observable siempre que sea posible para:
- Mejorar el rendimiento
- Simplificar el código
- Aprovechar la detección de cambios optimizada

```typescript
// RECOMENDADO: Usar signals
export class AuthService {
  currentUser = signal<User | null>(null);
  isAuthenticated = computed(() => !!this.currentUser());
  
  login(user: User) {
    this.currentUser.set(user);
  }
  
  logout() {
    this.currentUser.set(null);
  }
}

// EN COMPONENTE
@Component({
  template: `
    @if (authService.isAuthenticated()) {
      <p>Bienvenido, {{ authService.currentUser()?.name }}</p>
    }
  `
})
export default class MyComponent {
  constructor(public authService: AuthService) {}
}
```

## Herramientas y Dependencias

### Gestión de Paquetes
Usar Bun en lugar de npm o yarn para instalación de dependencias:

```bash
# RECOMENDADO
bun add zod @angular/material

# EVITAR
npm install zod @angular/material
```

### Validación de Formularios
- Usar Zod para la validación de esquemas y formularios
- Integrar Zod con Reactive Forms para tener un sistema de validación robusto

```typescript
// Definir esquema con Zod
const loginSchema = z.object({
  username: z.string().min(3),
  password: z.string().min(8)
});

// Validar en componente
validateFormWithZod() {
  try {
    loginSchema.parse(this.loginForm.value);
  } catch (error) {
    if (error instanceof z.ZodError) {
      // Manejar errores
    }
  }
}
```

## Rendimiento y Optimización

- Preferir la exportación por defecto para los componentes de página para optimizar la carga diferida
- Usar signals en lugar de observables para mejorar el rendimiento
- Implementar estrategias de memorización con `computed()` para cálculos costosos
- Usar `@defer` en plantillas para componentes pesados que puedan cargarse de forma diferida

---

## Principios Fundamentales

### Puertos (Interfaces)

#### Puertos de Entrada (Casos de Uso)
Definen las operaciones que la aplicación proporciona:

```java
public interface CreateAuctionUseCase {
    Long createAuction(CreateAuctionCommand command);
}
```

#### Puertos de Salida (Repositorios)
Definen cómo la aplicación se comunica con el exterior:

```java
public interface AuctionRepository {
    Auction save(Auction auction);
    Optional<Auction> findById(Long id);
    List<Auction> findAllByCategory(String category);
}
```

### Adaptadores

#### Adaptadores de Entrada (Web)
Implementan los puertos de entrada, reciben solicitudes y las transforman:

```java
@RestController
@RequestMapping("/api/auctions")
public class AuctionController {
    private final CreateAuctionUseCase createAuctionUseCase;
    
    @Autowired
    public AuctionController(CreateAuctionUseCase createAuctionUseCase) {
        this.createAuctionUseCase = createAuctionUseCase;
    }
    
    @PostMapping
    public ResponseEntity<Long> createAuction(@RequestBody CreateAuctionRequest request) {
        CreateAuctionCommand command = mapToCommand(request);
        Long auctionId = createAuctionUseCase.createAuction(command);
        return ResponseEntity.created(URI.create("/api/auctions/" + auctionId)).body(auctionId);
    }
}
```

#### Adaptadores de Salida (Persistencia)
Implementan los puertos de salida, convierten los modelos de dominio a entidades de persistencia:

```java
@Repository
public class AuctionJpaRepository implements AuctionRepository {
    private final SpringDataAuctionRepository repository;
    private final AuctionMapper mapper;
    
    @Autowired
    public AuctionJpaRepository(SpringDataAuctionRepository repository, AuctionMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    
    @Override
    public Auction save(Auction auction) {
        AuctionEntity entity = mapper.toEntity(auction);
        AuctionEntity savedEntity = repository.save(entity);
        return mapper.toDomain(savedEntity);
    }
}
```

## DTOs y Mappers

### Transferencia de Datos
Utilizar DTOs para la comunicación entre capas:

```java
public class CreateAuctionRequest {
    private String title;
    private String description;
    private BigDecimal startingPrice;
    // Getters y setters
}

public class CreateAuctionCommand {
    private final String title;
    private final String description;
    private final BigDecimal startingPrice;
    // Constructor y getters
}
```

### Mappers
Utilizar mappers para convertir entre DTOs, entidades de dominio y entidades de persistencia:

```java
@Component
public class AuctionMapper {
    public Auction toDomain(AuctionEntity entity) {
        return new Auction(
            entity.getId(),
            entity.getTitle(),
            entity.getDescription(),
            entity.getStartingPrice()
        );
    }
    
    public AuctionEntity toEntity(Auction domain) {
        AuctionEntity entity = new AuctionEntity();
        entity.setId(domain.getId());
        entity.setTitle(domain.getTitle());
        entity.setDescription(domain.getDescription());
        entity.setStartingPrice(domain.getStartingPrice());
        return entity;
    }
}
```

## Validación

### Validación de Entrada
Utilizar Bean Validation para validar las solicitudes:

```java
public class CreateAuctionRequest {
    @NotBlank(message = "El título es obligatorio")
    private String title;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, message = "La descripción debe tener al menos 20 caracteres")
    private String description;
    
    @NotNull(message = "El precio inicial es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio inicial debe ser mayor que 0")
    private BigDecimal startingPrice;
    // Getters y setters
}
```

### Validación de Dominio
Implementar validaciones de negocio en la capa de dominio:

```java
public class Auction {
    // Atributos
    
    public void validate() {
        if (endDate.isBefore(LocalDateTime.now())) {
            throw new InvalidAuctionException("La fecha de finalización debe ser posterior a la fecha actual");
        }
        if (reservePrice != null && reservePrice.compareTo(startingPrice) < 0) {
            throw new InvalidAuctionException("El precio de reserva no puede ser menor que el precio inicial");
        }
    }
}
```

## Seguridad

### JWT para Autenticación
Implementar seguridad basada en JWT:

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .oauth2ResourceServer(oauth2 -> oauth2.jwt())
            .build();
    }
}
```

## Testing

### Tests Unitarios
Test para la lógica de dominio y servicios:

```java
@ExtendWith(MockitoExtension.class)
public class CreateAuctionServiceTest {
    
    @Mock
    private AuctionRepository auctionRepository;
    
    @InjectMocks
    private CreateAuctionService createAuctionService;
    
    @Test
    void shouldCreateAuction() {
        // Arrange
        CreateAuctionCommand command = new CreateAuctionCommand(
            "Test Auction",
            "This is a test auction description",
            new BigDecimal("10.00")
        );
        
        // Act
        Long auctionId = createAuctionService.createAuction(command);
        
        // Assert
        verify(auctionRepository).save(any(Auction.class));
        assertNotNull(auctionId);
    }
}
```

### Tests de Integración
Test para verificar la integración entre componentes:

```java
@SpringBootTest
@AutoConfigureMockMvc
public class AuctionControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void shouldCreateAuction() throws Exception {
        // Arrange
        CreateAuctionRequest request = new CreateAuctionRequest();
        request.setTitle("Test Auction");
        request.setDescription("This is a test auction description");
        request.setStartingPrice(new BigDecimal("10.00"));
        
        // Act & Assert
        mockMvc.perform(post("/api/auctions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(asJsonString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$").isNumber());
    }
    
    private String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
```

---

Estas directrices se actualizarán a medida que el proyecto evolucione y se identifiquen nuevas mejores prácticas.
