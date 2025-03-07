package dev.mostapha.bidmaster.domain.model.auction;

/**
 * Enum que representa los posibles estados de una subasta.
 */
public enum AuctionStatus {
    /**
     * Borrador - La subasta está siendo creada pero no está abierta para pujas
     */
    DRAFT,
    
    /**
     * Activa - La subasta está publicada y aceptando pujas
     */
    ACTIVE,
    
    /**
     * Pausada - La subasta está temporalmente detenida
     */
    PAUSED,
    
    /**
     * Finalizada - La subasta ha concluido, ya sea con éxito o sin pujas ganadoras
     */
    FINISHED,
    
    /**
     * Cancelada - La subasta fue cancelada antes de su conclusión normal
     */
    CANCELLED
}
