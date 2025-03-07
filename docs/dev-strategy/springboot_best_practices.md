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
