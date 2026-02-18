CREATE TABLE gallery.comment_likes (
     id UUID PRIMARY KEY NOT NULL ,
     created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
     comment_id UUID NOT NULL,
     user_id UUID NOT NULL,

     CONSTRAINT fk_like_comment
         FOREIGN KEY (comment_id)
         REFERENCES gallery.comments(id)
         ON DELETE CASCADE,

     CONSTRAINT fk_like_comment_user
         FOREIGN KEY (user_id)
         REFERENCES gallery.social_users(id)
         ON DELETE CASCADE
);
