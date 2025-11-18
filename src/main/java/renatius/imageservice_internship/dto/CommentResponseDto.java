package renatius.imageservice_internship.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDto {
    private UUID id;
    private UUID imageId;
    private String description;
    private String username;
    private LocalDateTime createdAt;
    private long likesCount;
    private boolean likedByCurrentUser;
}
