package renatius.imageservice_internship.integration;
import org.junit.jupiter.api.Test;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProfileControllerIntegrationTest extends BaseIntegrationTest {

    @Test
    void getProfile_success() throws Exception {
        mockMvc.perform(
                        get("/profile/{id}", 1)
                                .header("X-User-Id", "00000000-0000-0000-0000-000000000002")
                                .header("X-User-Username", "user10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.images").isArray());
    }
}
