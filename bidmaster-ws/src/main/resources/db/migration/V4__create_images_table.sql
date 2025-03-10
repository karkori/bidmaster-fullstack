-- Creación de la tabla images
CREATE TABLE IF NOT EXISTS images (
    id UUID PRIMARY KEY,
    filename VARCHAR(255) NOT NULL,
    original_filename VARCHAR(255) NOT NULL,
    content_type VARCHAR(100) NOT NULL,
    size BIGINT NOT NULL,
    bucket_name VARCHAR(100) NOT NULL,
    object_name VARCHAR(255) NOT NULL,
    thumbnail_object_name VARCHAR(255) NOT NULL,
    upload_date TIMESTAMP NOT NULL,
    uploaded_by UUID,
    type VARCHAR(50) NOT NULL,
    reference_id UUID,
    version BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT fk_uploaded_by FOREIGN KEY (uploaded_by) REFERENCES users(id) ON DELETE SET NULL
);

-- Índices para mejorar el rendimiento de las consultas
CREATE INDEX idx_images_uploaded_by ON images(uploaded_by);
CREATE INDEX idx_images_reference_id_type ON images(reference_id, type);
