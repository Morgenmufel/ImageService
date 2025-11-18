package renatius.imageservice_internship.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import renatius.imageservice_internship.dto.*;
import renatius.imageservice_internship.repository.SocialUserRepository;
import renatius.imageservice_internship.security.SecurityConfig;
import renatius.imageservice_internship.service.*;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@WebMvcTest(controllers = ImageController.class)
@AutoConfigureMockMvc(addFilters = false)
class ImageControllerTest {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    @MockitoBean private ImageService imageService;
    @MockitoBean private CommentService commentService;
    @MockitoBean private LikeService likeService;
    @MockitoBean private SocialUserService socialUserService;
    @MockitoBean private SocialUserRepository socialUserRepository;

    @Test
    void getImageById_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        ImageResponseDto dto = new ImageResponseDto();
        Mockito.when(imageService.getImageById(id)).thenReturn(dto);
        mvc.perform(get("/api/images/{id}", id))
                .andExpect(status().isOk());
    }

    @Test
    void getProfile_returnsOk() throws Exception {
        UUID id = UUID.randomUUID();
        SocialUserResponseDto dto = new SocialUserResponseDto();
        Mockito.when(socialUserService.getUserProfile(eq(id), any(Pageable.class))).thenReturn(dto);

        mvc.perform(get("/api/profile/{userId}", id))
                .andExpect(status().isOk());
    }

    @Test
    void commentImage_postsAndReturns() throws Exception {
        UUID id = UUID.randomUUID();
        CommentImageDto in = new CommentImageDto();
        in.setDescription("hey");
        CommentResponseDto out = new CommentResponseDto();
        Mockito.when(commentService.addCommentToImage(eq(id), any())).thenReturn(out);

        mvc.perform(post("/api/images/{id}/comments", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(in)))
                .andExpect(status().isOk());
    }
}

