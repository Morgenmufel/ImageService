package renatius.imageservice_internship.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import renatius.imageservice_internship.dto.LikeResponseDto;
import renatius.imageservice_internship.entities.Image;
import renatius.imageservice_internship.entities.SocialUser;
import renatius.imageservice_internship.entities.CommentImage;
import renatius.imageservice_internship.entities.LikeImage;
import renatius.imageservice_internship.entities.LikeComment;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.repository.ImageRepository;
import renatius.imageservice_internship.repository.LikeCommentRepository;
import renatius.imageservice_internship.repository.LikeImageRepository;
import renatius.imageservice_internship.repository.CommentImageRepository;
import renatius.imageservice_internship.service.LikeService;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LikeServiceImpl implements LikeService {

    private final LikeImageRepository likeImageRepository;
    private final LikeCommentRepository likeCommentRepository;
    private final CommentImageRepository commentImageRepository;
    private final ImageRepository imageRepository;
    private final SecurityContextHolderUtil securityContextHolderUtil;

    @Override
    public LikeResponseDto toogleLikeToImage(UUID imageId) {
        SocialUser user = securityContextHolderUtil.getCurrentUser();
        Image image = imageRepository.findById(imageId)
                .orElseThrow(() -> new ImageNotFoundException("Image not found"));
        Optional<LikeImage> existingLike = likeImageRepository.findByImage_IdAndSocialUser_Id(imageId, user.getId());
        boolean liked;
        if (existingLike.isPresent()) {
            likeImageRepository.delete(existingLike.get());
            liked = false;
        } else {
            LikeImage newLike = LikeImage.builder()
                    .id(UUID.randomUUID())
                    .socialUser(user)
                    .image(image)
                    .build();
            likeImageRepository.save(newLike);
            liked = true;
        }
        long likesCount = likeImageRepository.countByImage_Id(imageId);
        return LikeResponseDto.builder()
                .targetId(imageId)
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }

    @Override
    public LikeResponseDto toogleLikeToComment(UUID commentId) {
        SocialUser user = securityContextHolderUtil.getCurrentUser();
        CommentImage comment = commentImageRepository.findById(commentId)
                .orElseThrow(() -> new CommentImageNotFoundException("Comment not found"));
        Optional<LikeComment> existingLike = likeCommentRepository.findByCommentImage_IdAndSocialUser_Id(commentId, user.getId());
        boolean liked;
        if (existingLike.isPresent()) {
            likeCommentRepository.delete(existingLike.get());
            liked = false;
        } else {
            LikeComment newLike = LikeComment.builder()
                    .id(UUID.randomUUID())
                    .socialUser(user)
                    .commentImage(comment)
                    .build();
            likeCommentRepository.save(newLike);
            liked = true;
        }
        long likesCount = likeCommentRepository.countByCommentImage_Id(commentId);
        return LikeResponseDto.builder()
                .targetId(commentId)
                .liked(liked)
                .likesCount(likesCount)
                .build();
    }
}
