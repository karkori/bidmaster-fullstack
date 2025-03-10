package dev.mostapha.bidmaster.adapter.in.web.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para la solicitud de creación de una nueva subasta
 */
public class CreateAuctionRequestDTO {
    
    @NotBlank(message = "El título es obligatorio")
    @Size(min = 5, max = 100, message = "El título debe tener entre 5 y 100 caracteres")
    private String title;
    
    @NotBlank(message = "La descripción es obligatoria")
    @Size(min = 20, max = 1000, message = "La descripción debe tener entre 20 y 1000 caracteres")
    private String description;
    
    @NotBlank(message = "La categoría es obligatoria")
    private String category;
    
    @NotNull(message = "El precio inicial es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio inicial debe ser mayor que 0")
    private BigDecimal initialPrice;
    
    @DecimalMin(value = "0", message = "El precio de reserva no puede ser negativo")
    private BigDecimal reservePrice;
    
    @NotNull(message = "La fecha de finalización es obligatoria")
    @Future(message = "La fecha de finalización debe ser en el futuro")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime endDate;
    
    @DecimalMin(value = "0.01", message = "El incremento mínimo debe ser mayor que 0")
    private BigDecimal minBidIncrement;
    
    @NotBlank(message = "La condición del artículo es obligatoria")
    private String condition;
    
    @NotBlank(message = "Las opciones de envío son obligatorias")
    private String shippingOptions;
    
    private boolean allowPause;
    
    // Constructores
    
    public CreateAuctionRequestDTO() {
        // Constructor por defecto necesario para la deserialización
    }

    // Getters y Setters
    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public BigDecimal getInitialPrice() {
        return initialPrice;
    }

    public void setInitialPrice(BigDecimal initialPrice) {
        this.initialPrice = initialPrice;
    }

    public BigDecimal getReservePrice() {
        return reservePrice;
    }

    public void setReservePrice(BigDecimal reservePrice) {
        this.reservePrice = reservePrice;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getMinBidIncrement() {
        return minBidIncrement;
    }

    public void setMinBidIncrement(BigDecimal minBidIncrement) {
        this.minBidIncrement = minBidIncrement;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getShippingOptions() {
        return shippingOptions;
    }

    public void setShippingOptions(String shippingOptions) {
        this.shippingOptions = shippingOptions;
    }

    public boolean isAllowPause() {
        return allowPause;
    }

    public void setAllowPause(boolean allowPause) {
        this.allowPause = allowPause;
    }
}
