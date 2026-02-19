package renatius.imageservice_internship.service;

import java.util.UUID;

public interface CommentCountView {
    UUID getCommentId();
    long getCnt();
}
