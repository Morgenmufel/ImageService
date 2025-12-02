package renatius.imageservice_internship.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ActivityEvent {
    private String userId;
    private String imageId;
    private LocalDateTime createdAt;
    private String status;
    private String type;
}
