-- Creación de la tabla auction_images
CREATE TABLE IF NOT EXISTS auction_images (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL,
    url VARCHAR(255) NOT NULL,
    description TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE
);

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_auction_images_auction_id ON auction_images(auction_id);
CREATE INDEX idx_auction_images_primary ON auction_images(auction_id, is_primary);
