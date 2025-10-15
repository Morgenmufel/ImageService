CREATE TABLE IF NOT EXISTS gallery.comments (
    id UUID PRIMARY KEY DEFAULT NOT NULL,
    description VARCHAR(500) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             user_id UUID NOT NULL,
                             image_id UUID NOT NULL,
                             CONSTRAINT fk_comments_user
                                 FOREIGN KEY (user_id)
                                 REFERENCES user_schema.users (id)
                                 ON DELETE CASCADE,
                             CONSTRAINT fk_comments_image
                                 FOREIGN KEY (image_id)
                                 REFERENCES gallery.images (id)
                                 ON DELETE CASCADE
);