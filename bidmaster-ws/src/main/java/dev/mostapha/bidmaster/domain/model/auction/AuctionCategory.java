package dev.mostapha.bidmaster.domain.model.auction;

import java.text.Normalizer;
import java.util.Locale;

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
     * Convierte un string (nombre de enum o displayName, insensible a mayúsculas y tildes)
     * a la enum correspondiente de forma segura, devolviendo null si no existe.
     */
    public static AuctionCategory fromString(String category) {
        if (category == null || category.isBlank()) {
            return null;
        }
        String normalizedInput = normalize(category);
        for (AuctionCategory value : values()) {
            if (normalize(value.name()).equals(normalizedInput)
                    || normalize(value.displayName).equals(normalizedInput)) {
                return value;
            }
        }
        return null;
    }

    private static String normalize(String value) {
        return Normalizer.normalize(value.trim(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase(Locale.ROOT);
    }
}
