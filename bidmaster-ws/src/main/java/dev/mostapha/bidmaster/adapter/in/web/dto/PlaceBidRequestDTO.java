package dev.mostapha.bidmaster.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * DTO para la solicitud de una nueva puja en una subasta
 */
public class PlaceBidRequestDTO {
    
    @NotNull(message = "El monto de la puja es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto de la puja debe ser mayor que 0")
    private BigDecimal amount;
    
    // Constructores
    
    public PlaceBidRequestDTO() {
        // Constructor por defecto necesario para la deserialización
    }
    
    public PlaceBidRequestDTO(BigDecimal amount) {
        this.amount = amount;
    }
    
    // Getters y Setters
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
}
