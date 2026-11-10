package dev.mostapha.bidmaster.concurrency;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import dev.mostapha.bidmaster.application.port.out.AuctionRepository;
import dev.mostapha.bidmaster.application.port.out.UserRepository;
import dev.mostapha.bidmaster.application.service.AuctionService;
import dev.mostapha.bidmaster.domain.model.auction.Auction;
import dev.mostapha.bidmaster.domain.model.auction.AuctionCategory;
import dev.mostapha.bidmaster.domain.model.user.User;
import dev.mostapha.bidmaster.domain.model.user.UserRole;
import dev.mostapha.bidmaster.domain.model.user.UserStatus;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("Pruebas de Concurrencia: Optimistic Locking en Pujas")
class ConcurrencyBidTest {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    private User seller;
    private User bidder1;
    private User bidder2;
    private Auction auction;

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 6);
        seller = User.create("seller_" + suffix, "seller_" + suffix + "@test.com", "hash", UserRole.SELLER);
        seller.addBalance(new BigDecimal("1000.00"));
        seller = userRepository.save(seller);

        bidder1 = User.create("bidder1_" + suffix, "bidder1_" + suffix + "@test.com", "hash", UserRole.BUYER);
        bidder1.addBalance(new BigDecimal("5000.00"));
        bidder1 = userRepository.save(bidder1);

        bidder2 = User.create("bidder2_" + suffix, "bidder2_" + suffix + "@test.com", "hash", UserRole.BUYER);
        bidder2.addBalance(new BigDecimal("5000.00"));
        bidder2 = userRepository.save(bidder2);

        Auction created = Auction.create(
                "Subasta Concurrente " + suffix,
                "Descripcion para prueba de concurrencia",
                AuctionCategory.ELECTRONICS,
                new BigDecimal("100.00"),
                LocalDateTime.now().plusDays(2),
                seller.getId());
        created.setMinBidIncrement(new BigDecimal("10.00"));
        created.publish();
        auction = auctionRepository.save(created);
    }

    @Test
    @DisplayName("Dos pujas simultáneas al mismo monto deben resolverse con éxito para 1 sola y fallo seguro para la otra")
    void shouldHandleConcurrentBidsWithOptimisticLocking() throws InterruptedException {
        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch endGate = new CountDownLatch(threadCount);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Hilo 1: bidder1 puja 120.00
        executor.submit(() -> {
            try {
                startGate.await();
                auctionService.placeBid(auction.getId(), bidder1.getId(), new BigDecimal("120.00"));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endGate.countDown();
            }
        });

        // Hilo 2: bidder2 puja 120.00
        executor.submit(() -> {
            try {
                startGate.await();
                auctionService.placeBid(auction.getId(), bidder2.getId(), new BigDecimal("120.00"));
                successCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            } finally {
                endGate.countDown();
            }
        });

        startGate.countDown(); // Dispara ambos hilos simultáneamente
        boolean finished = endGate.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(finished, "Los hilos de puja debieron terminar en menos de 10 segundos");
        assertEquals(1, successCount.get(), "Exactamente una puja concurrente debe tener éxito");
        assertEquals(1, failureCount.get(), "Exactamente una puja concurrente debe fallar por conflicto/regla");

        // Verificar consistencia del estado persistido
        Auction updatedAuction = auctionRepository.findById(auction.getId()).orElseThrow();
        assertEquals(1, updatedAuction.getTotalBids());
        assertEquals(new BigDecimal("120.00"), updatedAuction.getCurrentPrice());
        assertTrue(updatedAuction.getWinnerId().equals(bidder1.getId()) || updatedAuction.getWinnerId().equals(bidder2.getId()));
    }
}
