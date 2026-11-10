-- Creación de la tabla bids (historial transaccional de pujas y depósitos bloqueados)
CREATE TABLE IF NOT EXISTS bids (
    id UUID PRIMARY KEY,
    auction_id UUID NOT NULL,
    bidder_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    deposit_blocked DECIMAL(19, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    CONSTRAINT fk_bids_auction FOREIGN KEY (auction_id) REFERENCES auctions(id) ON DELETE CASCADE,
    CONSTRAINT fk_bids_bidder FOREIGN KEY (bidder_id) REFERENCES users(id)
);

CREATE INDEX idx_bids_auction_id ON bids(auction_id, created_at DESC);
CREATE INDEX idx_bids_bidder_id ON bids(bidder_id, created_at DESC);
CREATE INDEX idx_bids_status ON bids(auction_id, status);
