CREATE TABLE gallery.image_likes (
    id UUID PRIMARY KEY NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    image_id UUID NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_like_image
        FOREIGN KEY (image_id)
        REFERENCES gallery.images(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_like_user
        FOREIGN KEY (user_id)
        REFERENCES gallery.social_users(id)
        ON DELETE CASCADE
);