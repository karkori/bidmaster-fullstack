-- Creación de la tabla auctions
CREATE TABLE IF NOT EXISTS auctions (
    id UUID PRIMARY KEY,
    title VARCHAR(100) NOT NULL,
    description TEXT,
    slug VARCHAR(150) UNIQUE,
    category VARCHAR(50) NOT NULL,
    initial_price DECIMAL(19, 2) NOT NULL,
    current_price DECIMAL(19, 2) NOT NULL,
    min_bid_increment DECIMAL(19, 2) NOT NULL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    total_bids INT NOT NULL DEFAULT 0,
    last_bid_date TIMESTAMP,
    condition VARCHAR(50),
    shipping_options TEXT,
    allow_pause BOOLEAN NOT NULL DEFAULT FALSE,
    seller_id UUID NOT NULL,
    winner_id UUID,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_seller FOREIGN KEY (seller_id) REFERENCES users(id),
    CONSTRAINT fk_winner FOREIGN KEY (winner_id) REFERENCES users(id)
);

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_auctions_seller ON auctions(seller_id);
CREATE INDEX idx_auctions_status ON auctions(status);
CREATE INDEX idx_auctions_category ON auctions(category);
CREATE INDEX idx_auctions_end_date ON auctions(end_date);
