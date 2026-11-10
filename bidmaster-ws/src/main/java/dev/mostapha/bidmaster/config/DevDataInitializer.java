package dev.mostapha.bidmaster.config;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import dev.mostapha.bidmaster.application.port.in.AuctionUseCase;
import dev.mostapha.bidmaster.application.port.in.ImageUseCase;
import dev.mostapha.bidmaster.application.port.in.UserUseCase;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.image.Image;
import dev.mostapha.bidmaster.domain.model.user.Address;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

/**
 * Inicializador idempotente de datos de demostración para el perfil "dev".
 */
@Configuration
@Profile("dev")
public class DevDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(DevDataInitializer.class);

    @Bean
    public CommandLineRunner initDevData(
            UserUseCase userUseCase,
            AuctionUseCase auctionUseCase,
            ImageUseCase imageUseCase) {
        return args -> {
            if (!userUseCase.findAllUsers().isEmpty()) {
                log.info("La base de datos ya contiene datos de desarrollo. Omitiendo inicialización.");
                return;
            }

            log.info("Inicializando datos de ejemplo para entorno de desarrollo...");
            createDemoUsers(userUseCase);
            createDemoAuctions(auctionUseCase, userUseCase, imageUseCase);
            log.info("¡Datos de ejemplo cargados y publicados con éxito!");
        };
    }

    private void createDemoUsers(UserUseCase userUseCase) {
        List<User> demoUsers = List.of(
                User.reconstitute(
                        UUID.randomUUID(),
                        "admin",
                        "admin@bidmaster.com",
                        "admin123",
                        "Admin",
                        "BidMaster",
                        "+34600123456",
                        new Address("Calle Admin", "Madrid", "Madrid", "28001", "España"),
                        UserStatus.ACTIVE,
                        UserRole.ADMIN,
                        null,
                        0,
                        new BigDecimal("0.00"),
                        new BigDecimal("0.00"),
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0L),
                User.reconstitute(
                        UUID.randomUUID(),
                        "vendedor",
                        "vendedor@example.com",
                        "vendedor123",
                        "Carlos",
                        "Vendedor",
                        "+34600111222",
                        new Address("Calle Comercio 23", "Barcelona", "Cataluña", "08001", "España"),
                        UserStatus.ACTIVE,
                        UserRole.USER,
                        null,
                        0,
                        new BigDecimal("5000.00"),
                        new BigDecimal("0.00"),
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0L),
                User.reconstitute(
                        UUID.randomUUID(),
                        "comprador",
                        "comprador@example.com",
                        "comprador123",
                        "Ana",
                        "Compradora",
                        "+34600333444",
                        new Address("Calle Compra 42", "Sevilla", "Andalucía", "41001", "España"),
                        UserStatus.ACTIVE,
                        UserRole.USER,
                        null,
                        0,
                        new BigDecimal("15000.00"),
                        new BigDecimal("0.00"),
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0L),
                User.reconstitute(
                        UUID.randomUUID(),
                        "especial",
                        "especial@example.com",
                        "especial123",
                        "Laura",
                        "Especial",
                        "+34600555666",
                        new Address("Avenida Coleccionista 7", "Valencia", "Valencia", "46001", "España"),
                        UserStatus.ACTIVE,
                        UserRole.USER,
                        null,
                        0,
                        new BigDecimal("25000.00"),
                        new BigDecimal("0.00"),
                        50,
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        0L)
        );

        for (User user : demoUsers) {
            User saved = userUseCase.registerUser(user);
            log.info("Usuario creado: {}", saved.getUsername());
        }
    }

    private void createDemoAuctions(
            AuctionUseCase auctionUseCase,
            UserUseCase userUseCase,
            ImageUseCase imageUseCase) {

        User seller = userUseCase.findUserByUsername("vendedor").orElseThrow();
        User buyer = userUseCase.findUserByUsername("comprador").orElseThrow();
        UUID sellerId = seller.getId();

        List<Auction> demoAuctions = List.of(
                Auction.create(
                        "MacBook Pro M4 Max - 36GB RAM",
                        "Portátil MacBook Pro en excelente estado con chip M4 Max, 36GB de memoria unificada y 1TB SSD. Incluye cargador original MagSafe y caja.",
                        AuctionCategory.ELECTRONICS,
                        new BigDecimal("2100.00"),
                        LocalDateTime.now().plusDays(7),
                        sellerId),
                Auction.create(
                        "Bolso de diseñador edición limitada",
                        "Bolso de diseñador edición limitada en cuero italiano de primera calidad. Incluye certificado de autenticidad y funda guardapolvo.",
                        AuctionCategory.FASHION,
                        new BigDecimal("500.00"),
                        LocalDateTime.now().plusDays(5),
                        sellerId),
                Auction.create(
                        "Figura coleccionable numerada (23/500)",
                        "Figura de colección numerada de edición limitada. Estado impecable en su caja original precintada con certificado.",
                        AuctionCategory.COLLECTIBLES,
                        new BigDecimal("300.00"),
                        LocalDateTime.now().plusDays(10),
                        sellerId),
                Auction.create(
                        "Pintura al óleo original - Paisaje Alpino",
                        "Obra original pintada al óleo sobre lienzo de lino (60x80cm), firmada por el autor y enmarcada en madera natural.",
                        AuctionCategory.ART,
                        new BigDecimal("400.00"),
                        LocalDateTime.now().plusDays(12),
                        sellerId),
                Auction.create(
                        "Bicicleta de montaña Carbono Pro XT",
                        "Bicicleta de montaña de gama alta con cuadro íntegro de carbono, suspensión Fox Factory y transmisión Shimano XT de 12 velocidades.",
                        AuctionCategory.SPORTS,
                        new BigDecimal("1800.00"),
                        LocalDateTime.now().plusDays(6),
                        sellerId)
        );

        demoAuctions.get(0).setCondition("Usado - Como nuevo");
        demoAuctions.get(0).setShippingOptions("Envío nacional gratuito asegurado");

        demoAuctions.get(1).setCondition("Nuevo con etiquetas");
        demoAuctions.get(1).setShippingOptions("Envío internacional disponible");

        demoAuctions.get(2).setCondition("Nuevo en caja");
        demoAuctions.get(2).setShippingOptions("Envío asegurado");

        demoAuctions.get(3).setCondition("Original - Firmado por el artista");
        demoAuctions.get(3).setShippingOptions("Embalaje especial de galería");

        demoAuctions.get(4).setCondition("Usado - Excelente estado");
        demoAuctions.get(4).setShippingOptions("Envío especializado o recogida en persona");

        for (int i = 0; i < demoAuctions.size(); i++) {
            Auction created = auctionUseCase.createAuction(demoAuctions.get(i));
            addAuctionImages(created, imageUseCase);
            Auction published = auctionUseCase.publishAuction(created.getId());

            // Añadir una puja inicial de ejemplo en las dos primeras subastas
            if (i < 2) {
                BigDecimal sampleBidAmount = published.calculateMinNextBid();
                auctionUseCase.placeBid(published.getId(), buyer.getId(), sampleBidAmount);
            }

            log.info("Subasta publicada: {} (ID: {})", published.getTitle(), published.getId());
        }
    }

    private void addAuctionImages(Auction auction, ImageUseCase imageUseCase) {
        List<String> imageUrls = switch (auction.getCategory()) {
            case ELECTRONICS -> List.of(
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800&auto=format&fit=crop");
            case FASHION -> List.of(
                    "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1575032617751-6ddec2089882?w=800&auto=format&fit=crop");
            case COLLECTIBLES -> List.of(
                    "https://images.unsplash.com/photo-1566576912321-d58ddd7a6088?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1591017683108-7312b2046494?w=800&auto=format&fit=crop");
            case ART -> List.of(
                    "https://images.unsplash.com/photo-1579783902614-a3fb3927b6a5?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1577720643272-6c6bb9d4d468?w=800&auto=format&fit=crop");
            case SPORTS -> List.of(
                    "https://images.unsplash.com/photo-1541625602330-2277a4c46182?w=800&auto=format&fit=crop",
                    "https://images.unsplash.com/photo-1505236269363-844e758cd3f2?w=800&auto=format&fit=crop");
            default -> List.of(
                    "https://images.unsplash.com/photo-1531297484001-80022131f5a1?w=800&auto=format&fit=crop");
        };

        for (int idx = 0; idx < imageUrls.size(); idx++) {
            Image image = Image.createAuctionUrlImage(
                    auction.getId(),
                    imageUrls.get(idx),
                    "image-" + (idx + 1) + ".jpg",
                    "image/jpeg",
                    1024L * 1024L,
                    idx == 0,
                    idx,
                    auction.getTitle());
            imageUseCase.saveImageMetadata(image);
        }
    }
}
