package dev.mostapha.bidmaster.domain.model.auction;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entidad de dominio Subasta.
 * Contiene la lógica de negocio y los invariantes del dominio para subastas.
 */
public class Auction {

    // Identificación
    private final UUID id;
    private String title;
    private String description;
    private String slug;
    
    // Configuración
    private BigDecimal initialPrice;
    private BigDecimal currentPrice;
    private BigDecimal minBidIncrement;
    private BigDecimal depositRequired;
    private boolean allowPause;
    private int pausesUsed;
    
    // Temporización
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private LocalDateTime pauseDate;
    private Duration pauseDuration;
    
    // Estado
    private AuctionStatus status;
    private UUID sellerId;
    private UUID winnerId;
    private int totalBids;
    private LocalDateTime lastBidDate;
    
    // Categorización
    private AuctionCategory category;
    private String condition;
    private String shippingOptions;
    
    // Campos de auditoría
    private final LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private long version;
    
    /**
     * Constructor privado para crear una nueva subasta
     */
    private Auction(UUID id, String title, String description, AuctionCategory category, 
                  BigDecimal initialPrice, LocalDateTime endDate, UUID sellerId) {
        this.id = id != null ? id : UUID.randomUUID();
        this.title = title;
        this.description = description;
        this.category = category;
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.endDate = endDate;
        this.sellerId = sellerId;
        
        // Valores por defecto
        this.status = AuctionStatus.DRAFT;
        this.totalBids = 0;
        this.slug = generateSlug(title);
        this.minBidIncrement = calculateDefaultBidIncrement(initialPrice);
        this.depositRequired = BigDecimal.ZERO;
        this.allowPause = false;
        this.pausesUsed = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        this.version = 0L;
    }
    

    /**
     * Factory method para crear una nueva subasta
     */
    public static Auction create(String title, String description, AuctionCategory category, 
                                BigDecimal initialPrice, LocalDateTime endDate, UUID sellerId) {
        // Validaciones
        if (title == null || title.length() < 5 || title.length() > 100) {
            throw new IllegalArgumentException("El título debe tener entre 5 y 100 caracteres");
        }
        if (description == null || description.length() < 20 || description.length() > 1000) {
            throw new IllegalArgumentException("La descripción debe tener entre 20 y 1000 caracteres");
        }
        if (category == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio inicial debe ser mayor que cero");
        }
        if (endDate == null || endDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("La fecha de fin debe ser al menos 1 hora después del momento actual");
        }
        if (sellerId == null) {
            throw new IllegalArgumentException("El ID del vendedor no puede ser nulo");
        }
        
        return new Auction(null, title, description, category, initialPrice, endDate, sellerId);
    }
    
    /**
     * Factory method para reconstruir una subasta existente desde persistencia
     */
    public static Auction reconstitute(UUID id, String title, String description, String slug,
                                     BigDecimal initialPrice, BigDecimal currentPrice, 
                                     BigDecimal minBidIncrement, BigDecimal depositRequired,
                                     boolean allowPause, int pausesUsed, 
                                     LocalDateTime startDate, LocalDateTime endDate,
                                     LocalDateTime pauseDate, Duration pauseDuration,
                                     AuctionStatus status, UUID sellerId, UUID winnerId,
                                     int totalBids, LocalDateTime lastBidDate, 
                                     AuctionCategory category, String condition, String shippingOptions,
                                     LocalDateTime createdAt, LocalDateTime updatedAt, long version) {
        // Creamos instancia básica
        Auction auction = new Auction(id, title, description, category, initialPrice, endDate, sellerId);
        
        // Actualizamos todos los campos
        auction.currentPrice = currentPrice;
        auction.slug = slug;
        auction.minBidIncrement = minBidIncrement;
        auction.depositRequired = depositRequired;
        auction.allowPause = allowPause;
        auction.pausesUsed = pausesUsed;
        auction.startDate = startDate;
        auction.pauseDate = pauseDate;
        auction.pauseDuration = pauseDuration;
        auction.status = status;
        auction.winnerId = winnerId;
        auction.totalBids = totalBids;
        auction.lastBidDate = lastBidDate;
        auction.condition = condition;
        auction.shippingOptions = shippingOptions;
        
        // No modificamos createdAt, pero actualizamos los demás campos de auditoría
        auction.updatedAt = updatedAt;
        auction.version = version;
        
        return auction;
    }
    
    // Métodos de negocio
    
    /**
     * Publica la subasta, cambiando su estado de DRAFT a ACTIVE
     */
    public void publish() {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("Solo se pueden publicar subastas en estado borrador");
        }
        
        // Verificar que tenga toda la información necesaria
        if (this.title == null || this.description == null || this.category == null ||
            this.initialPrice == null || this.endDate == null) {
            throw new IllegalStateException("No se puede publicar una subasta incompleta");
        }
        
        this.status = AuctionStatus.ACTIVE;
        this.startDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Coloca una puja en la subasta
     * @return true si la puja fue exitosa, false si no cumple con las condiciones
     */
    public boolean placeBid(UUID bidderId, BigDecimal amount) {
        // Validar precondiciones
        if (!canBid(bidderId, amount)) {
            return false;
        }
        
        // Actualizar estado
        this.currentPrice = amount;
        this.winnerId = bidderId;
        this.totalBids++;
        this.lastBidDate = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        
        return true;
    }
    
    /**
     * Verifica si es posible realizar una puja
     */
    public boolean canBid(UUID bidderId, BigDecimal amount) {
        // No se permite pujar en subastas no activas
        if (this.status != AuctionStatus.ACTIVE) {
            return false;
        }
        
        // No se permite que el vendedor puje en su propia subasta
        if (bidderId.equals(this.sellerId)) {
            return false;
        }
        
        // La subasta no debe haber finalizado
        if (isFinished()) {
            return false;
        }
        
        // El monto debe ser mayor que el precio actual + incremento mínimo
        BigDecimal minAcceptableBid = this.currentPrice.add(this.minBidIncrement);
        if (amount.compareTo(minAcceptableBid) < 0) {
            return false;
        }
        
        return true;
    }
    
    /**
     * Finaliza la subasta, cambiando su estado a FINISHED
     */
    public void finish() {
        if (this.status != AuctionStatus.ACTIVE && this.status != AuctionStatus.PAUSED) {
            throw new IllegalStateException("Solo se pueden finalizar subastas activas o pausadas");
        }
        
        this.status = AuctionStatus.FINISHED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Cancela la subasta, cambiando su estado a CANCELLED
     */
    public void cancel() {
        if (this.status == AuctionStatus.FINISHED || this.status == AuctionStatus.CANCELLED) {
            throw new IllegalStateException("No se pueden cancelar subastas ya finalizadas o canceladas");
        }
        
        this.status = AuctionStatus.CANCELLED;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Pausa la subasta si está permitido
     */
    public void pause() {
        if (!canPause()) {
            throw new IllegalStateException("No se puede pausar la subasta");
        }
        
        this.status = AuctionStatus.PAUSED;
        this.pauseDate = LocalDateTime.now();
        this.pausesUsed++;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Reanuda una subasta pausada
     */
    public void resume() {
        if (this.status != AuctionStatus.PAUSED) {
            throw new IllegalStateException("Solo se pueden reanudar subastas pausadas");
        }
        
        // Calcular la duración de la pausa
        Duration pauseDuration = Duration.between(this.pauseDate, LocalDateTime.now());
        this.pauseDuration = pauseDuration;
        
        // Extender la fecha de finalización por la duración de la pausa
        this.endDate = this.endDate.plus(pauseDuration);
        
        // Actualizar estado
        this.status = AuctionStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * Verifica si la subasta puede ser pausada
     */
    public boolean canPause() {
        return this.status == AuctionStatus.ACTIVE && 
               this.allowPause && 
               this.pausesUsed < 2;
    }
    
    /**
     * Verifica si la subasta ha finalizado basado en el tiempo
     */
    public boolean isFinished() {
        return this.status == AuctionStatus.FINISHED || 
               this.status == AuctionStatus.CANCELLED ||
               (this.status == AuctionStatus.ACTIVE && LocalDateTime.now().isAfter(this.endDate));
    }
    
    /**
     * Calcula el tiempo restante de la subasta
     */
    public Duration getTimeLeft() {
        if (isFinished()) {
            return Duration.ZERO;
        }
        
        if (this.status == AuctionStatus.PAUSED) {
            return Duration.between(LocalDateTime.now(), this.pauseDate.plus(Duration.between(this.pauseDate, this.endDate)));
        }
        
        return Duration.between(LocalDateTime.now(), this.endDate);
    }
    
    /**
     * Calcula el monto mínimo para la siguiente puja
     */
    public BigDecimal calculateMinNextBid() {
        return this.currentPrice.add(this.minBidIncrement);
    }
    
    /**
     * Genera un slug a partir del título
     */
    private String generateSlug(String title) {
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s]", "")
                .replaceAll("\\s+", "-") 
                + "-" + UUID.randomUUID().toString().substring(0, 8);
    }
    
    /**
     * Calcula el incremento mínimo predeterminado basado en el precio inicial
     */
    private BigDecimal calculateDefaultBidIncrement(BigDecimal initialPrice) {
        if (initialPrice.compareTo(new BigDecimal("10")) <= 0) {
            return new BigDecimal("0.5");
        } else if (initialPrice.compareTo(new BigDecimal("100")) <= 0) {
            return new BigDecimal("1");
        } else if (initialPrice.compareTo(new BigDecimal("1000")) <= 0) {
            return new BigDecimal("5");
        } else {
            return new BigDecimal("10");
        }
    }
    
    // Getters y setters para modificaciones seguras
    
    public UUID getId() {
        return id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar el título de una subasta publicada");
        }
        if (title == null || title.length() < 5 || title.length() > 100) {
            throw new IllegalArgumentException("El título debe tener entre 5 y 100 caracteres");
        }
        this.title = title;
        this.slug = generateSlug(title);
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar la descripción de una subasta publicada");
        }
        if (description == null || description.length() < 20 || description.length() > 1000) {
            throw new IllegalArgumentException("La descripción debe tener entre 20 y 1000 caracteres");
        }
        this.description = description;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getSlug() {
        return slug;
    }
    
    public BigDecimal getInitialPrice() {
        return initialPrice;
    }
    
    public void setInitialPrice(BigDecimal initialPrice) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar el precio inicial de una subasta publicada");
        }
        if (initialPrice == null || initialPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El precio inicial debe ser mayor que cero");
        }
        this.initialPrice = initialPrice;
        this.currentPrice = initialPrice;
        this.minBidIncrement = calculateDefaultBidIncrement(initialPrice);
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getCurrentPrice() {
        return currentPrice;
    }
    
    public BigDecimal getMinBidIncrement() {
        return minBidIncrement;
    }
    
    public void setMinBidIncrement(BigDecimal minBidIncrement) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar el incremento mínimo de una subasta publicada");
        }
        if (minBidIncrement == null || minBidIncrement.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("El incremento mínimo debe ser mayor que cero");
        }
        this.minBidIncrement = minBidIncrement;
        this.updatedAt = LocalDateTime.now();
    }
    
    public BigDecimal getDepositRequired() {
        return depositRequired;
    }
    
    public void setDepositRequired(BigDecimal depositRequired) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar el depósito requerido de una subasta publicada");
        }
        if (depositRequired == null || depositRequired.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El depósito requerido no puede ser negativo");
        }
        this.depositRequired = depositRequired;
        this.updatedAt = LocalDateTime.now();
    }
    
    public boolean isAllowPause() {
        return allowPause;
    }
    
    public void setAllowPause(boolean allowPause) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar la configuración de pausas de una subasta publicada");
        }
        this.allowPause = allowPause;
        this.updatedAt = LocalDateTime.now();
    }
    
    public int getPausesUsed() {
        return pausesUsed;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar la fecha de fin de una subasta publicada");
        }
        if (endDate == null || endDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new IllegalArgumentException("La fecha de fin debe ser al menos 1 hora después del momento actual");
        }
        this.endDate = endDate;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getPauseDate() {
        return pauseDate;
    }
    
    public Duration getPauseDuration() {
        return pauseDuration;
    }
    
    public AuctionStatus getStatus() {
        return status;
    }
    
    public UUID getSellerId() {
        return sellerId;
    }
    
    public UUID getWinnerId() {
        return winnerId;
    }
    
    public int getTotalBids() {
        return totalBids;
    }
    
    public LocalDateTime getLastBidDate() {
        return lastBidDate;
    }
    
    public AuctionCategory getCategory() {
        return category;
    }
    
    public void setCategory(AuctionCategory category) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar la categoría de una subasta publicada");
        }
        if (category == null) {
            throw new IllegalArgumentException("La categoría no puede ser nula");
        }
        this.category = category;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getCondition() {
        return condition;
    }
    
    public void setCondition(String condition) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se puede modificar la condición de una subasta publicada");
        }
        this.condition = condition;
        this.updatedAt = LocalDateTime.now();
    }
    
    public String getShippingOptions() {
        return shippingOptions;
    }
    
    public void setShippingOptions(String shippingOptions) {
        if (this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("No se pueden modificar las opciones de envío de una subasta publicada");
        }
        this.shippingOptions = shippingOptions;
        this.updatedAt = LocalDateTime.now();
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public long getVersion() {
        return version;
    }
}
