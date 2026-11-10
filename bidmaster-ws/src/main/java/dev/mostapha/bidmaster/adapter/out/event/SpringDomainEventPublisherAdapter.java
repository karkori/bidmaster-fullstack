package dev.mostapha.bidmaster.adapter.out.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import dev.mostapha.bidmaster.application.port.out.DomainEventPublisherPort;
import dev.mostapha.bidmaster.domain.event.AuctionStatusChangedEvent;
import dev.mostapha.bidmaster.domain.event.BidPlacedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Adaptador de salida que publica los eventos de dominio a través del bus transaccional
 * de eventos de Spring (ApplicationEventPublisher).
 *
 * Al estar detrás del puerto hexagonal DomainEventPublisherPort, si en el futuro
 * el sistema se divide en microservicios, basta con añadir un KafkaDomainEventPublisherAdapter
 * sin tocar una sola línea del dominio ni de los casos de uso.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SpringDomainEventPublisherAdapter implements DomainEventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    @Override
    public void publishBidPlaced(BidPlacedEvent event) {
        log.debug("Publicando evento de dominio BidPlacedEvent: auctionId={}, amount={}",
                event.auctionId(), event.amount());
        applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishAuctionStatusChanged(AuctionStatusChangedEvent event) {
        log.debug("Publicando evento de dominio AuctionStatusChangedEvent: auctionId={}, status={}",
                event.auctionId(), event.newStatus());
        applicationEventPublisher.publishEvent(event);
    }
}
