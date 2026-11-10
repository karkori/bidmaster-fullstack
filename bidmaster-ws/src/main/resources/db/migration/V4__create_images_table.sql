-- Tabla unificada de imágenes (reemplaza la duplicación entre auction_images e images)
CREATE TABLE IF NOT EXISTS images (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL DEFAULT 0,
    url VARCHAR(500),
    thumbnail_url VARCHAR(500),
    bucket_name VARCHAR(100),
    object_name VARCHAR(255),
    thumbnail_object_name VARCHAR(255),
    description TEXT,
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    display_order INT NOT NULL DEFAULT 0,
    upload_date TIMESTAMP NOT NULL,
    uploaded_by UUID,
    type VARCHAR(50) NOT NULL,
    reference_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

CREATE INDEX idx_images_uploaded_by ON images(uploaded_by);
CREATE INDEX idx_images_reference_id_type ON images(reference_id, type, display_order);
CREATE INDEX idx_images_primary ON images(reference_id, type, is_primary);
