package renatius.imageservice_internship.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import renatius.imageservice_internship.dto.*;
import renatius.imageservice_internship.service.CommentService;
import renatius.imageservice_internship.service.ImageService;
import renatius.imageservice_internship.service.LikeService;
import renatius.imageservice_internship.service.SocialUserService;

import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final CommentService commentService;
    private final LikeService likeService;
    private final SocialUserService socialUserService;

    @PostMapping(value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> uploadImage(
            @ModelAttribute ImageUploadRequest imageUploadRequest) {
        System.out.println("in Controller");
        return ResponseEntity.ok(imageService.uploadSingleImage(imageUploadRequest));
    }

    @GetMapping("/profile/{userId}")
    public ResponseEntity<SocialUserResponseDto> getProfile(@PathVariable UUID userId,
                                                            Pageable pageable) {
        return ResponseEntity.ok(socialUserService.getUserProfile(userId, pageable));
    }

    @GetMapping("/images/{id}")
    public ResponseEntity<ImageResponseDto> getImageById(@PathVariable UUID id) {
        return ResponseEntity.ok(imageService.getImageById(id));
    }

    @GetMapping("/user/{id}/images")
    public ResponseEntity<PagedResponseDto<ImageResponseDto>> getImagesByUser(@PathVariable UUID id,
                                                                              Pageable pageable) {
        return ResponseEntity.ok(imageService.getImagesByUserPaginated(id, pageable));
    }

    @GetMapping("/images")
    public ResponseEntity<PagedResponseDto<ImageResponseDto>> getAllImages(Pageable pageable) {
        return ResponseEntity.ok(imageService.getAllImagesPaginated(pageable));
    }

    @PostMapping("/images/{id}/likes")
    public ResponseEntity<LikeResponseDto> likeImage(@PathVariable UUID id) {
        return ResponseEntity.ok(likeService.toogleLikeToImage(id));
    }

    @PostMapping("/images/{id}/comments")
    public ResponseEntity<CommentResponseDto> commentImage(@PathVariable UUID id,
                                                           @RequestBody CommentImageDto commentImageDto) {
        return ResponseEntity.ok(commentService.addCommentToImage(id, commentImageDto));
    }

    @DeleteMapping("/images/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(@PathVariable UUID commentId) {
        commentService.removeCommentFromImage(commentId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/images/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable UUID commentId,
            @RequestBody CommentImageDto commentImageDto){
        return ResponseEntity.ok(commentService.updateCommentImage(commentId, commentImageDto));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponseDto> likeComment(@PathVariable UUID commentId) {
        return ResponseEntity.ok(likeService.toogleLikeToComment(commentId));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        imageService.deleteImageById(imageId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
