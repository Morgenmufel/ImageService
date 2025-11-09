package renatius.imageservice_internship.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "comments")
@Builder
public class CommentImage {

    @Id
    private UUID id;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SocialUser user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.REMOVE,
            mappedBy = "commentImage"
    )
    private List<LikeComment> commentLikes;
}
