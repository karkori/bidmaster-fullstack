package dev.mostapha.bidmaster.domain.model.bid;

/**
 * Estados posibles de una puja en una subasta.
 */
public enum BidStatus {
    /**
     * Puja líder actual de una subasta activa
     */
    WINNING,

    /**
     * Puja que ha sido superada por otra mayor
     */
    OUTBID,

    /**
     * Puja ganadora al finalizar la subasta
     */
    WON,

    /**
     * Puja cancelada (p. ej. si se cancela la subasta)
     */
    CANCELLED
}
