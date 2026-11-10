package dev.mostapha.bidmaster.application.port.out;

import dev.mostapha.bidmaster.domain.event.AuctionStatusChangedEvent;
import dev.mostapha.bidmaster.domain.event.BidPlacedEvent;

/**
 * Puerto de salida (Hexagonal) para la publicación de eventos de dominio.
 * Permite desacoplar la lógica de negocio del mecanismo de transporte
 * (Eventos transaccionales de Spring en un monolito modular, o Kafka/RabbitMQ en microservicios).
 */
public interface DomainEventPublisherPort {

    void publishBidPlaced(BidPlacedEvent event);

    void publishAuctionStatusChanged(AuctionStatusChangedEvent event);
}
