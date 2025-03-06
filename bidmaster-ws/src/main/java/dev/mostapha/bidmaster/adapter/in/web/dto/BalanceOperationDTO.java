package dev.mostapha.bidmaster.adapter.in.web.dto;

import java.math.BigDecimal;

/**
 * DTO para operaciones de balance (depositar, retirar, bloquear, desbloquear)
 */
public class BalanceOperationDTO {
    
    private BigDecimal amount;
    private String description;
    
    // Getters y Setters
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
}
