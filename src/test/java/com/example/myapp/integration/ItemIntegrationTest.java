package com.example.myapp.integration;

import com.example.myapp.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtTokenProvider tokenProvider;

    private String authHeader() {
        return "Bearer " + tokenProvider.generateToken("testuser");
    }

    @Test
    void fullCrudFlow() throws Exception {
        // Create
        var result = mockMvc.perform(post("/api/v1/items")
                        .header("Authorization", authHeader())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Integration Item", "description": "Test"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Item"))
                .andReturn();

        var body = result.getResponse().getContentAsString();
        var id = com.fasterxml.jackson.databind.json.JsonMapper.builder().build()
                .readTree(body).get("id").asText();

        // Read
        mockMvc.perform(get("/api/v1/items/" + id)
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Integration Item"));

        // List
        mockMvc.perform(get("/api/v1/items")
                        .header("Authorization", authHeader()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/items"))
                .andExpect(status().isUnauthorized());
    }
}
