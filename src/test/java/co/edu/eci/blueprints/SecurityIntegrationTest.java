package co.edu.eci.blueprints;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private String loginAndGetToken(String username, String password) throws Exception {
        String body = """
                {
                    "username": "%s",
                    "password": "%s"
                }
                """.formatted(username, password);

        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return jsonNode.get("access_token").asText();
    }

    @Test
    @DisplayName("Endpoints públicos deben ser accesibles sin token")
    void publicEndpointsAccessibleWithoutAuth() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Endpoints protegidos deben retornar 401 si no se envía token")
    void protectedEndpointsReturn401WithoutToken() throws Exception {
        mockMvc.perform(get("/api/v1/blueprints"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Login con credenciales incorrectas debe retornar 401")
    void invalidLoginReturns401() throws Exception {
        String body = """
                {
                    "username": "student",
                    "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("Assistant debe tener solo scope blueprints.read y poder hacer GET")
    void assistantCanRead() throws Exception {
        String token = loginAndGetToken("assistant", "assistant123");
        assertNotNull(token);

        // GET debe retornar 200 OK
        mockMvc.perform(get("/api/v1/blueprints")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Assistant NO debe tener permiso de escritura (POST retorna 403)")
    void assistantCannotWrite() throws Exception {
        String token = loginAndGetToken("assistant", "assistant123");
        assertNotNull(token);

        String newBlueprintJson = """
                {
                    "author": "assistant_author",
                    "name": "assistant_bp",
                    "points": [{"x": 10, "y": 20}]
                }
                """;

        mockMvc.perform(post("/api/v1/blueprints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBlueprintJson))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Student debe tener scope blueprints.write y poder crear blueprint")
    void studentCanWrite() throws Exception {
        String token = loginAndGetToken("student", "student123");
        assertNotNull(token);

        String uniqueName = "bp_" + System.currentTimeMillis();
        String newBlueprintJson = """
                {
                    "author": "student_author",
                    "name": "%s",
                    "points": [{"x": 15, "y": 25}]
                }
                """.formatted(uniqueName);

        mockMvc.perform(post("/api/v1/blueprints")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBlueprintJson))
                .andExpect(status().isCreated());
    }
}
