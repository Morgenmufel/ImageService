package renatius.imageservice_internship.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ImageControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void uploadImage_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image".getBytes()
        );


        mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "Example")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists());
    }

    @Test
    void likeImage_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image".getBytes()
        );
        String json = mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "Example")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        UUID imageId = UUID.fromString(mapper.readTree(json).get("id").asText());
        mockMvc.perform(
                        post("/api/images/{id}/likes", imageId)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isOk());
    }

    @Test
    void getImages_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "fake image".getBytes()
        );
        mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "Example")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.url").exists());

        mockMvc.perform(
                        get("/api/images?page=0&size=5")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void deleteImage_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );
        String json = mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "desc")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        UUID imageId = UUID.fromString(mapper.readTree(json).get("id").asText());
        mockMvc.perform(
                        delete("/api/images/{id}", imageId)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "testuser")
                )
                .andExpect(status().isNoContent());
    }

}

