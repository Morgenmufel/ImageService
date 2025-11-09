package renatius.imageservice_internship.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import renatius.imageservice_internship.dto.*;
import renatius.imageservice_internship.service.CommentService;
import renatius.imageservice_internship.service.ImageService;
import renatius.imageservice_internship.service.LikeService;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageService imageService;
    private final CommentService commentService;
    private final LikeService likeService;

    @PostMapping(value = "/images",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ImageResponseDto> uploadImage(
            @ModelAttribute ImageUploadRequest imageUploadRequest) {
        return ResponseEntity.ok(imageService.uploadSingleImage(imageUploadRequest));
    }

    @PostMapping("/images/{id}")
    public ResponseEntity<ImageResponseDto> getImageById(@PathVariable UUID id) {
        return ResponseEntity.ok(imageService.getImageById(id));
    }

    @PostMapping("/user/{id}/images")
    public ResponseEntity<PagedResponseDto<ImageResponseDto>> getImagesByUser(@PathVariable UUID id,
                                                                              @RequestParam(defaultValue = "0") int page,
                                                                              @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(imageService.getImagesByUserPaginated(id, page, size));
    }

    @GetMapping("/images")
    public ResponseEntity<PagedResponseDto<ImageResponseDto>> getAllImages(@RequestParam(defaultValue = "0") int page,
                                                                           @RequestParam(defaultValue = "9") int size) {
        return ResponseEntity.ok(imageService.getAllImagesPaginated(page, size));
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
        boolean res = commentService.removeCommentFromImage(commentId);
        if (res){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    @PostMapping("/images/comments/{commentId}")
    public ResponseEntity<CommentResponseDto> updateComment(
            @PathVariable UUID commentId,
            @RequestBody CommentImageDto commentImageDto
    ){
        return ResponseEntity.ok(commentService.updateCommentImage(commentId, commentImageDto));
    }

    @PostMapping("/comments/{commentId}/likes")
    public ResponseEntity<LikeResponseDto> likeComment(@PathVariable UUID commentId) {
        return ResponseEntity.ok(likeService.toogleLikeToComment(commentId));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> deleteImage(@PathVariable UUID imageId) {
        boolean res = imageService.deleteImageById(imageId);
        if (res){
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
