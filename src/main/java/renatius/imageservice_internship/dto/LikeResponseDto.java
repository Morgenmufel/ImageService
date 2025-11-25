package renatius.imageservice_internship.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LikeResponseDto {
    private UUID targetId;
    private boolean liked;
    private long likesCount;
}