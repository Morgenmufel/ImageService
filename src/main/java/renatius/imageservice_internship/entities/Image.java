package renatius.imageservice_internship.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.JoinColumn;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "images")
@Builder
public class Image {

    @Id
    private UUID id;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private SocialUser user;

    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.REMOVE,
            mappedBy = "image")
    private List<LikeImage> imageLikes;

    @OneToMany(
            fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.REMOVE,
            mappedBy = "image"
    )
    private List<CommentImage> imageComments;
}
