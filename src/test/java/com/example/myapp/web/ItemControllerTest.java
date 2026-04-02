package com.example.myapp.web;

import com.example.myapp.config.SecurityConfig;
import com.example.myapp.domain.entity.Item;
import com.example.myapp.domain.exception.ResourceNotFoundException;
import com.example.myapp.security.JwtAuthFilter;
import com.example.myapp.security.JwtTokenProvider;
import com.example.myapp.service.ItemService;
import com.example.myapp.web.controller.ItemController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ItemController.class)
@Import({SecurityConfig.class, JwtAuthFilter.class})
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ItemService itemService;

    @MockitoBean
    private JwtTokenProvider tokenProvider;

    @Test
    void create_withInvalidBody_shouldReturn400() throws Exception {
        given(tokenProvider.isValid("test-token")).willReturn(true);
        given(tokenProvider.getSubject("test-token")).willReturn("user");

        mockMvc.perform(post("/api/v1/items")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}")
                        .with(csrf()))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));
    }

    @Test
    void get_notFound_shouldReturn404() throws Exception {
        var id = UUID.randomUUID();
        given(tokenProvider.isValid("test-token")).willReturn(true);
        given(tokenProvider.getSubject("test-token")).willReturn("user");
        given(itemService.findById(id)).willThrow(new ResourceNotFoundException("Item", id));

        mockMvc.perform(get("/api/v1/items/" + id)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("NOT_FOUND"));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        var item = Item.builder()
                .id(UUID.randomUUID())
                .title("Test")
                .description("Desc")
                .build();
        given(tokenProvider.isValid("test-token")).willReturn(true);
        given(tokenProvider.getSubject("test-token")).willReturn("user");
        given(itemService.create("Test", "Desc")).willReturn(item);

        mockMvc.perform(post("/api/v1/items")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title": "Test", "description": "Desc"}
                                """)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test"));
    }
}
