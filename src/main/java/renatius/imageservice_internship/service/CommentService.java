package renatius.imageservice_internship.service;

import renatius.imageservice_internship.dto.CommentImageDto;
import renatius.imageservice_internship.dto.CommentResponseDto;
import java.util.List;
import java.util.UUID;

public interface CommentService {
    CommentResponseDto addCommentToImage(UUID imageId, CommentImageDto commentImageDto);
    CommentResponseDto updateCommentImage(UUID commentId, CommentImageDto commentImageDto);
    boolean removeCommentFromImage(UUID commentId);
    List<CommentResponseDto> getCommentsByImage(UUID imageId);
}
