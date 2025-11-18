CREATE TABLE gallery.social_users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE
);
