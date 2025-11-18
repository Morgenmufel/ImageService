package renatius.imageservice_internship.entities;

import jakarta.persistence.Id;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.FetchType;
import jakarta.persistence.CascadeType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "social_users")
@Builder
public class SocialUser {

    @Id
    private UUID id;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @OneToMany(fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE,
            orphanRemoval = true, mappedBy = "user")
    private List<Image> images;

    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.REMOVE, mappedBy = "socialUser")
    private List<LikeComment> likeComments;

    @OneToMany(fetch = FetchType.LAZY,
            orphanRemoval = true,
            cascade = CascadeType.REMOVE, mappedBy = "socialUser")
    private List<LikeImage> likeImages;

    @OneToMany(fetch = FetchType.LAZY,
        orphanRemoval = true,
        cascade = CascadeType.REMOVE, mappedBy = "user")
    private List<CommentImage> commentImages;

}
