package renatius.imageservice_internship.service;

import renatius.imageservice_internship.dto.LikeResponseDto;

import java.util.UUID;

public interface LikeService {
    LikeResponseDto toogleLikeToImage(UUID imageId);
    LikeResponseDto toogleLikeToComment(UUID commentId);
}