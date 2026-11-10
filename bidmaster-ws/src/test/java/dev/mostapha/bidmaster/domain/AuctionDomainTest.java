package dev.mostapha.bidmaster.domain;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.auction.AuctionStatus;

@DisplayName("Pruebas Unitarias de Dominio: Auction")
class AuctionDomainTest {

    private UUID sellerId;
    private UUID bidderId1;
    private UUID bidderId2;

    @BeforeEach
    void setUp() {
        sellerId = UUID.randomUUID();
        bidderId1 = UUID.randomUUID();
        bidderId2 = UUID.randomUUID();
    }

    @Test
    @DisplayName("Debe crear una subasta en estado DRAFT con slug e incrementos calculados según tramos")
    void shouldCreateAuctionInDraftStatusWithCalculatedTiers() {
        // Tramo 1: <= 10 -> incremento 0.5
        Auction a1 = Auction.create("Subasta Item Barato", "Descripcion detallada", AuctionCategory.ELECTRONICS,
                new BigDecimal("8.00"), LocalDateTime.now().plusDays(2), sellerId);
        assertEquals(AuctionStatus.DRAFT, a1.getStatus());
        assertEquals(new BigDecimal("0.5"), a1.getMinBidIncrement());
        assertNotNull(a1.getSlug());
        assertTrue(a1.getSlug().contains("subasta-item-barato"));

        // Tramo 2: <= 100 -> incremento 1
        Auction a2 = Auction.create("Subasta Reloj Vintage", "Descripcion detallada", AuctionCategory.FASHION,
                new BigDecimal("50.00"), LocalDateTime.now().plusDays(2), sellerId);
        assertEquals(new BigDecimal("1"), a2.getMinBidIncrement());

        // Tramo 3: <= 1000 -> incremento 5
        Auction a3 = Auction.create("Subasta Laptop Gaming", "Descripcion detallada", AuctionCategory.ELECTRONICS,
                new BigDecimal("500.00"), LocalDateTime.now().plusDays(2), sellerId);
        assertEquals(new BigDecimal("5"), a3.getMinBidIncrement());

        // Tramo 4: > 1000 -> incremento 10
        Auction a4 = Auction.create("Subasta Coche Clasico", "Descripcion detallada", AuctionCategory.VEHICLES,
                new BigDecimal("5000.00"), LocalDateTime.now().plusDays(2), sellerId);
        assertEquals(new BigDecimal("10"), a4.getMinBidIncrement());
    }

    @Test
    @DisplayName("Debe rechazar creación de subasta con datos inválidos")
    void shouldRejectInvalidAuctionCreation() {
        // Título corto (< 5 chars)
        assertThrows(IllegalArgumentException.class, () ->
                Auction.create("Abc", "Desc", AuctionCategory.OTHERS, new BigDecimal("10.00"),
                        LocalDateTime.now().plusDays(1), sellerId));

        // Precio inicial <= 0
        assertThrows(IllegalArgumentException.class, () ->
                Auction.create("Titulo Valido", "Desc", AuctionCategory.OTHERS, new BigDecimal("0.00"),
                        LocalDateTime.now().plusDays(1), sellerId));

        // Fecha fin en el pasado
        assertThrows(IllegalArgumentException.class, () ->
                Auction.create("Titulo Valido", "Desc", AuctionCategory.OTHERS, new BigDecimal("10.00"),
                        LocalDateTime.now().minusDays(1), sellerId));
    }

    @Test
    @DisplayName("Debe normalizar tildes y caracteres especiales en la generación de slug")
    void shouldNormalizeAccentsInSlugGeneration() {
        Auction auction = Auction.create("¡Subasta de Móviles & Cámaras!", "Descripción con tildes",
                AuctionCategory.ELECTRONICS, new BigDecimal("100.00"), LocalDateTime.now().plusDays(3), sellerId);

        assertTrue(auction.getSlug().contains("subasta-de-moviles-camaras"));
        assertFalse(auction.getSlug().contains("ó"));
        assertFalse(auction.getSlug().contains("á"));
        assertFalse(auction.getSlug().contains("!"));
    }

    @Test
    @DisplayName("Debe gestionar el ciclo de vida: publicar, pausar, reanudar y límite de 2 pausas")
    void shouldManageAuctionLifecycleAndPauseLimit() throws InterruptedException {
        Auction auction = Auction.create("Subasta Camara Reflex", "Descripcion de prueba",
                AuctionCategory.COLLECTIBLES, new BigDecimal("100.00"), LocalDateTime.now().plusHours(5), sellerId);
        auction.setAllowPause(true);

        // No se puede pausar en DRAFT
        assertFalse(auction.canPause());

        // Publicar
        auction.publish();
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
        assertNotNull(auction.getStartDate());

        // Pausa 1
        assertTrue(auction.canPause());
        auction.pause();
        assertEquals(AuctionStatus.PAUSED, auction.getStatus());
        assertEquals(1, auction.getPausesUsed());

        // Reanudar
        LocalDateTime oldEndDate = auction.getEndDate();
        Thread.sleep(50); // pequeña pausa para verificar extensión
        auction.resume();
        assertEquals(AuctionStatus.ACTIVE, auction.getStatus());
        assertTrue(auction.getEndDate().isAfter(oldEndDate) || auction.getEndDate().isEqual(oldEndDate));

        // Pausa 2
        assertTrue(auction.canPause());
        auction.pause();
        assertEquals(2, auction.getPausesUsed());
        auction.resume();

        // 3ra pausa no debe estar permitida (límite máximo 2)
        assertFalse(auction.canPause());
        assertThrows(IllegalStateException.class, auction::pause);
    }

    @Test
    @DisplayName("Debe validar reglas de puja en el dominio")
    void shouldValidateBiddingRules() {
        Auction auction = Auction.create("Subasta Consola Retro", "Descripcion completa y detallada de la consola retro",
                AuctionCategory.TOYS, new BigDecimal("100.00"), LocalDateTime.now().plusDays(1), sellerId);
        auction.setMinBidIncrement(new BigDecimal("10.00"));

        // En DRAFT no se puede pujar
        assertFalse(auction.canBid(bidderId1, new BigDecimal("120.00")));

        auction.publish();

        // Vendedor no puede autopujar
        assertFalse(auction.canBid(sellerId, new BigDecimal("120.00")));

        // Puja inferior al precio inicial + incremento (100 + 10 = 110)
        assertFalse(auction.canBid(bidderId1, new BigDecimal("105.00")));

        // Puja válida de 110
        assertTrue(auction.canBid(bidderId1, new BigDecimal("110.00")));
        boolean placed1 = auction.placeBid(bidderId1, new BigDecimal("110.00"));
        assertTrue(placed1);
        assertEquals(new BigDecimal("110.00"), auction.getCurrentPrice());
        assertEquals(bidderId1, auction.getWinnerId());
        assertEquals(1, auction.getTotalBids());

        // Siguiente puja debe ser >= 120
        assertFalse(auction.canBid(bidderId2, new BigDecimal("115.00")));
        assertTrue(auction.canBid(bidderId2, new BigDecimal("130.00")));
        boolean placed2 = auction.placeBid(bidderId2, new BigDecimal("130.00"));
        assertTrue(placed2);
        assertEquals(new BigDecimal("130.00"), auction.getCurrentPrice());
        assertEquals(bidderId2, auction.getWinnerId());
        assertEquals(2, auction.getTotalBids());

        // Finalizar y verificar que no se aceptan más pujas
        auction.finish();
        assertEquals(AuctionStatus.FINISHED, auction.getStatus());
        assertFalse(auction.canBid(bidderId1, new BigDecimal("200.00")));
    }
}
