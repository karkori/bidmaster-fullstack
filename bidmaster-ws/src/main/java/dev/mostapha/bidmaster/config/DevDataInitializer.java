package dev.mostapha.bidmaster.config;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.AuctionImageUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionImage;
import dev.mostapha.bidmaster.domain.model.user.Address;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Clase de configuración que inicializa datos de ejemplo para el entorno de desarrollo.
 * Se ejecuta solo cuando el perfil activo es "dev".
 */
@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);
    
    /**
     * Bean que ejecuta la inicialización de datos al arrancar la aplicación.
     */
    @Bean
    public CommandLineRunner initDevData(UserUseCase userUseCase, AuctionUseCase auctionUseCase, AuctionImageUseCase auctionImageUseCase) {
        return args -> {
            log.info("Inicializando datos de ejemplo para entorno de desarrollo...");
            
            // Crear usuarios de ejemplo
            createDemoUsers(userUseCase)
                .thenMany(createDemoAuctions(auctionUseCase, userUseCase, auctionImageUseCase))
                .doOnComplete(() -> log.info("¡Datos de ejemplo cargados con éxito!"))
                .subscribe();
        };
    }
    
    /**
     * Crea usuarios de ejemplo para pruebas.
     */
    private Flux<User> createDemoUsers(UserUseCase userUseCase) {
        log.info("Creando usuarios de ejemplo...");
        
        // Lista de usuarios de ejemplo - Crear usuarios básicos
        // Nota: El método reconstitute debe usarse ya que necesitamos asignar información personal
        List<User> demoUsers = List.of(
            User.reconstitute(
                UUID.randomUUID(),
                "admin",
                "admin@bidmaster.com",
                "admin123",
                "Admin",
                "BidMaster",
                "+34600123456",
                new Address("Calle Admin", "1", "28001", "Madrid", "España"),
                UserStatus.ACTIVE,
                UserRole.ADMIN,
                null,
                0,
                new BigDecimal("0.00"),
                new BigDecimal("0.00"),
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
            ),
            
            User.reconstitute(
                UUID.randomUUID(),
                "vendedor",
                "vendedor@example.com",
                "vendedor123",
                "Carlos",
                "Vendedor",
                "+34600111222",
                new Address("Calle Comercio", "23", "08001", "Barcelona", "España"),
                UserStatus.ACTIVE,
                UserRole.USER,
                null,
                0,
                new BigDecimal("5000.00"),
                new BigDecimal("0.00"),
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
            ),
            
            User.reconstitute(
                UUID.randomUUID(),
                "comprador",
                "comprador@example.com",
                "comprador123",
                "Ana",
                "Compradora",
                "+34600333444",
                new Address("Calle Compra", "42", "41001", "Sevilla", "España"),
                UserStatus.ACTIVE,
                UserRole.USER,
                null,
                0,
                new BigDecimal("2000.00"),
                new BigDecimal("0.00"),
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
            ),
            
            User.reconstitute(
                UUID.randomUUID(),
                "especial",
                "especial@example.com",
                "especial123",
                "Laura",
                "Especial",
                "+34600555666",
                new Address("Avenida Coleccionista", "7", "46001", "Valencia", "España"),
                UserStatus.ACTIVE,
                UserRole.USER,
                null,
                0,
                new BigDecimal("10000.00"),
                new BigDecimal("0.00"),
                50,
                LocalDateTime.now(),
                LocalDateTime.now(),
                0L
            )
        );
        
        // Guardar usuarios en la base de datos
        return Flux.fromIterable(demoUsers)
            .flatMap(userUseCase::registerUser)
            .doOnNext(user -> log.info("Usuario creado: {}", user.getUsername()));
    }
    
    /**
     * Crea subastas de ejemplo para pruebas.
     */
    private Flux<Auction> createDemoAuctions(AuctionUseCase auctionUseCase, UserUseCase userUseCase, AuctionImageUseCase auctionImageUseCase) {
        log.info("Creando subastas de ejemplo...");
        
        // Obtener el ID del vendedor
        return userUseCase.findUserByUsername("vendedor")
            .flatMapMany(seller -> {
                UUID sellerId = seller.getId();
                
                // Lista de subastas de ejemplo con diferentes categorías y estados
                List<Auction> demoAuctions = List.of(
                    // Subasta de electrónica
                    Auction.create(
                        "MacBook Pro 2023 - 16GB RAM",
                        "Portátil MacBook Pro 2023 en excelente estado, con 16GB de RAM y 512GB SSD. " +
                        "Incluye cargador original y funda protectora.",
                        AuctionCategory.ELECTRONICS,
                        new BigDecimal("1200.00"),
                        LocalDateTime.now().plusDays(7),
                        sellerId
                    ),
                    
                    // Subasta de moda
                    Auction.create(
                        "Bolso de diseñador exclusivo",
                        "Bolso de diseñador edición limitada. Cuero italiano de primera calidad, " +
                        "diseño único y certificado de autenticidad incluido.",
                        AuctionCategory.FASHION,
                        new BigDecimal("500.00"),
                        LocalDateTime.now().plusDays(5),
                        sellerId
                    ),
                    
                    // Subasta de coleccionables
                    Auction.create(
                        "Figura coleccionable edición limitada",
                        "Figura de colección numerada (23/500). Edición limitada de personaje legendario. " +
                        "Incluye caja original y certificado de autenticidad.",
                        AuctionCategory.COLLECTIBLES,
                        new BigDecimal("300.00"),
                        LocalDateTime.now().plusDays(10),
                        sellerId
                    ),
                    
                    // Subasta de arte
                    Auction.create(
                        "Pintura al óleo original - Paisaje montañoso",
                        "Pintura al óleo original de artista emergente. Paisaje montañoso con lago, " +
                        "60x80cm, enmarcado con marco de madera natural.",
                        AuctionCategory.ART,
                        new BigDecimal("400.00"),
                        LocalDateTime.now().plusDays(12),
                        sellerId
                    ),
                    
                    // Subasta de deportes
                    Auction.create(
                        "Bicicleta de montaña profesional",
                        "Bicicleta de montaña de gama alta con cuadro de carbono, suspensión Fox, " +
                        "cambios Shimano XT y frenos hidráulicos. Usada en solo 3 salidas.",
                        AuctionCategory.SPORTS,
                        new BigDecimal("2000.00"),
                        LocalDateTime.now().plusDays(6),
                        sellerId
                    )
                );
                
                // Añadir condiciones y opciones de envío después de crear las subastas
                demoAuctions.get(0).setCondition("Usado - Como nuevo");
                demoAuctions.get(0).setShippingOptions("Envío nacional gratuito");
                
                demoAuctions.get(1).setCondition("Nuevo con etiquetas");
                demoAuctions.get(1).setShippingOptions("Envío internacional disponible");
                
                demoAuctions.get(2).setCondition("Nuevo en caja");
                demoAuctions.get(2).setShippingOptions("Envío asegurado");
                
                demoAuctions.get(3).setCondition("Original - Firmado por el artista");
                demoAuctions.get(3).setShippingOptions("Recogida en persona recomendada");
                
                demoAuctions.get(4).setCondition("Usado - Excelente estado");
                demoAuctions.get(4).setShippingOptions("Solo recogida en persona");
                
                // Guardar subastas en la base de datos
                return Flux.fromIterable(demoAuctions)
                    .flatMap(auction -> auctionUseCase.createAuction(auction)
                        .flatMap(savedAuction -> addAuctionImages(savedAuction, auctionImageUseCase))
                    )
                    .doOnNext(auction -> log.info("Subasta creada: {} (ID: {})", auction.getTitle(), auction.getId()));
            });
    }
    
    /**
     * Añade imágenes a una subasta.
     */
    private Mono<Auction> addAuctionImages(Auction auction, AuctionImageUseCase auctionImageUseCase) {
        List<String> imageUrls;
        
        // Asignar URLs de imágenes según la categoría
        switch (auction.getCategory()) {
            case ELECTRONICS:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&auto=format&fit=crop"
                );
                break;
            case FASHION:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1575032617751-6ddec2089882?w=800&auto=format&fit=crop"
                );
                break;
            case COLLECTIBLES:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1591017683108-7312b2046494?w=800&auto=format&fit=crop"
                );
                break;
            case ART:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1577720643272-6c6bb9d4d468?w=800&auto=format&fit=crop"
                );
                break;
            case SPORTS:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1505236269363-844e758cd3f2?w=800&auto=format&fit=crop"
                );
                break;
            default:
                imageUrls = List.of(
                    "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=800&auto=format&fit=crop"
                );
                break;
        }
        
        // Crear y guardar imágenes para la subasta
        return Flux.fromIterable(imageUrls)
            .index()
            .flatMap(tuple -> {
                long index = tuple.getT1();
                String url = tuple.getT2();
                boolean isPrimary = (index == 0); // La primera imagen es la principal
                String fileName = "image-" + (index + 1) + ".jpg";
                String contentType = "image/jpeg";
                long fileSize = 1024 * 1024; // 1MB aprox
                
                // Crear la imagen con los parámetros correctos
                AuctionImage image = AuctionImage.create(
                    auction.getId(),
                    url,
                    fileName,
                    contentType,
                    fileSize
                );
                
                // Establecer si es la imagen principal
                if (isPrimary) {
                    image.setAsPrimary();
                }
                
                // Establecer orden de visualización
                image.setDisplayOrder((int) index);
                
                // Guardar la imagen usando el caso de uso específico para imágenes
                return auctionImageUseCase.saveAuctionImage(image);
            })
            .then(Mono.just(auction));
    }
}
