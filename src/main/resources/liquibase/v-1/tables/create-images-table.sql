CREATE TABLE gallery.images (
    id UUID PRIMARY KEY NOT NULL,
    url TEXT NOT NULL,
    description TEXT,
    uploaded_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    user_id UUID NOT NULL,
    CONSTRAINT fk_image_user
        FOREIGN KEY (user_id)
        REFERENCES gallery.social_users(id)
        ON DELETE CASCADE
);
