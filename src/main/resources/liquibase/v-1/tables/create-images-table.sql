CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE IF NOT EXISTS gallery.images (
                                              id UUID PRIMARY KEY DEFAULT NOT NULL,
    url VARCHAR(500) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    uploaded_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              user_id UUID NOT NULL
                              CONSTRAINT fk_images_user
                                  FOREIGN KEY (user_id)
                                  REFERENCES user_schema.users (id)
                                  ON DELETE CASCADE
                              );