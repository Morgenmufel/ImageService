package renatius.imageservice_internship.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import renatius.imageservice_internship.entities.*;
import renatius.imageservice_internship.exceptions.CommentImageNotFoundException;
import renatius.imageservice_internship.exceptions.ImageNotFoundException;
import renatius.imageservice_internship.repository.*;
import renatius.imageservice_internship.service.impl.LikeServiceImpl;
import renatius.imageservice_internship.util.SecurityContextHolderUtil;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class LikeServiceImplTest {

    @Mock private LikeImageRepository likeImageRepository;
    @Mock private LikeCommentRepository likeCommentRepository;
    @Mock private CommentImageRepository commentImageRepository;
    @Mock private ImageRepository imageRepository;
    @Mock private SecurityContextHolderUtil securityContextHolderUtil;

    @InjectMocks private LikeServiceImpl likeService;

    private SocialUser user;
    private Image image;
    private CommentImage comment;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        user = SocialUser.builder().id(UUID.randomUUID()).username("u").build();
        image = Image.builder().id(UUID.randomUUID()).build();
        comment = CommentImage.builder().id(UUID.randomUUID()).build();
    }

}

