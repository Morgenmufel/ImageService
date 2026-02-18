package renatius.imageservice_internship.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImageResponseDto {
    private UUID id;
    private UUID userId;
    private String url;
    private String description;
    private String username;
    private LocalDateTime uploadedAt;
    private long likesCount;
    private long commentsCount;
    private boolean likedByCurrentUser;
    private List<CommentResponseDto> comments;
}
