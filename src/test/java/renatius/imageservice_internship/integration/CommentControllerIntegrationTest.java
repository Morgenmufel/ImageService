package renatius.imageservice_internship.integration;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CommentControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void createComment_success() throws Exception {


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

        String body = """
                {
                  "description": "Nice!"
                }
                """;

        mockMvc.perform(
                        post("/api/images/{id}/comments", imageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Nice!"));

    }

    @Test
    void updateComment_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file",
                "a.jpg",
                MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );

        String json = mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "desc")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        ObjectMapper mapper = new ObjectMapper();
        UUID imageId = UUID.fromString(mapper.readTree(json).get("id").asText());

        String commentBody = """
        { "description": "old comment" }
        """;

        String commentJson = mockMvc.perform(
                        post("/api/images/{id}/comments", imageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(commentBody)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID commentId = UUID.fromString(mapper.readTree(commentJson).get("id").asText());

        String updateBody = """
        { "description": "updated!" }
        """;

        mockMvc.perform(
                        post("/api/images/comments/{cid}", commentId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(updateBody)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("updated!"));
    }

    @Test
    void deleteComment_success() throws Exception {

        MockMultipartFile file = new MockMultipartFile(
                "file", "a.jpg", MediaType.IMAGE_JPEG_VALUE, "img".getBytes()
        );

        String json = mockMvc.perform(
                        multipart("/api/images")
                                .file(file)
                                .param("description", "desc")
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();
        ObjectMapper mapper = new ObjectMapper();
        UUID imageId = UUID.fromString(mapper.readTree(json).get("id").asText());

        String create = """
        { "description": "test" }
        """;

        String commentJson = mockMvc.perform(
                        post("/api/images/{id}/comments", imageId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(create)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andReturn()
                .getResponse()
                .getContentAsString();

        UUID commentId = UUID.fromString(mapper.readTree(commentJson).get("id").asText());

        mockMvc.perform(
                        delete("/api/images/comments/{cid}", commentId)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user2")
                )
                .andExpect(status().isNoContent());
    }


}
