package dev.mostapha.bidmaster.domain.model.auction;

/**
 * Enum que representa las categorías disponibles para clasificar las subastas.
 */
public enum AuctionCategory {
    ELECTRONICS("Electrónica"),
    FASHION("Moda"),
    HOME("Hogar y Jardín"),
    COLLECTIBLES("Coleccionables"),
    ART("Arte"),
    SPORTS("Deportes"),
    TOYS("Juguetes"),
    VEHICLES("Vehículos"),
    OTHERS("Otros");
    
    private final String displayName;
    
    AuctionCategory(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    /**
     * Convierte un string (posiblemente de entrada del usuario) a la enum correspondiente
     * de forma segura, devolviendo null si no existe.
     */
    public static AuctionCategory fromString(String category) {
        try {
            return category != null ? AuctionCategory.valueOf(category.toUpperCase()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
