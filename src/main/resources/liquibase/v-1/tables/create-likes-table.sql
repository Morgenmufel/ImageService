CREATE TABLE IF NOT EXISTS gallery.likes (
                                             id UUID PRIMARY KEY DEFAULT NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             user_id UUID NOT NULL,
                             image_id UUID,
                             comment_id UUID,
                             CONSTRAINT ck_like_target
                             CHECK (
                             (image_id IS NOT NULL AND comment_id IS NULL) OR
(image_id IS NULL AND comment_id IS NOT NULL)
    )
    CONSTRAINT fk_likes_user
        FOREIGN KEY (user_id)
        REFERENCES user_schema.users (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_likes_image
        FOREIGN KEY (image_id)
        REFERENCES gallery.images (id)
        ON DELETE CASCADE,
    CONSTRAINT fk_likes_comment
        FOREIGN KEY (comment_id)
        REFERENCES gallery.comments (id)
        ON DELETE CASCADE
    );
