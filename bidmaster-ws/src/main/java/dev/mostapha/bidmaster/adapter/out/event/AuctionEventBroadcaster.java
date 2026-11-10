package dev.mostapha.bidmaster.adapter.out.event;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import dev.mostapha.bidmaster.domain.event.AuctionStatusChangedEvent;
import dev.mostapha.bidmaster.domain.event.BidPlacedEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * Escucha los eventos de dominio una vez confirmada la transacción en PostgreSQL (AFTER_COMMIT)
 * y los transmite en tiempo real a los clientes conectados mediante Server-Sent Events (SSE).
 */
@Component
@Slf4j
public class AuctionEventBroadcaster {

    private static final long SSE_TIMEOUT_MS = 30 * 60 * 1000L; // 30 minutos

    private final Map<UUID, List<SseEmitter>> auctionEmitters = new ConcurrentHashMap<>();
    private final List<SseEmitter> globalEmitters = new CopyOnWriteArrayList<>();

    public SseEmitter subscribeToAuction(UUID auctionId) {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        List<SseEmitter> emitters = auctionEmitters.computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>());
        emitters.add(emitter);

        Runnable cleanup = () -> emitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    public SseEmitter subscribeToAllAuctions() {
        SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);
        globalEmitters.add(emitter);

        Runnable cleanup = () -> globalEmitters.remove(emitter);
        emitter.onCompletion(cleanup);
        emitter.onTimeout(cleanup);
        emitter.onError(e -> cleanup.run());

        return emitter;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onBidPlaced(BidPlacedEvent event) {
        log.info("Transacción confirmada - Nueva puja en subasta '{}' ({}): {} por {}",
                event.auctionTitle(), event.auctionId(), event.amount(), event.bidderUsername());

        broadcastToAuction(event.auctionId(), "bid-placed", event);
        broadcastToGlobal("bid-placed", event);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onAuctionStatusChanged(AuctionStatusChangedEvent event) {
        log.info("Transacción confirmada - Cambio de estado en subasta '{}' ({}): {}",
                event.auctionTitle(), event.auctionId(), event.newStatus());

        broadcastToAuction(event.auctionId(), "auction-status-changed", event);
        broadcastToGlobal("auction-status-changed", event);
    }

    private void broadcastToAuction(UUID auctionId, String eventName, Object payload) {
        List<SseEmitter> emitters = auctionEmitters.get(auctionId);
        if (emitters == null || emitters.isEmpty()) {
            return;
        }
        sendToEmitters(emitters, eventName, payload);
    }

    private void broadcastToGlobal(String eventName, Object payload) {
        if (globalEmitters.isEmpty()) {
            return;
        }
        sendToEmitters(globalEmitters, eventName, payload);
    }

    private void sendToEmitters(List<SseEmitter> emitters, String eventName, Object payload) {
        for (SseEmitter emitter : emitters) {
            try {
                emitter.send(SseEmitter.event()
                        .name(eventName)
                        .data(payload));
            } catch (IOException e) {
                emitter.complete();
                emitters.remove(emitter);
            }
        }
    }
}
